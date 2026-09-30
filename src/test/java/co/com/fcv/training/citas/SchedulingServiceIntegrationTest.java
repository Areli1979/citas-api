package co.com.fcv.training.citas;

import co.com.fcv.training.citas.application.SchedulingService;
import co.com.fcv.training.citas.application.SchedulingFailure;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
class SchedulingServiceIntegrationTest {
    @Container static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4");
    @DynamicPropertySource static void properties(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", MYSQL::getJdbcUrl); r.add("spring.datasource.username", MYSQL::getUsername); r.add("spring.datasource.password", MYSQL::getPassword);
        r.add("app.jwt.access-secret", () -> "a".repeat(40)); r.add("app.jwt.refresh-secret", () -> "b".repeat(40)); r.add("app.cookie.secure", () -> true); r.add("app.cookie.same-site", () -> "None");
    }
    @Autowired SchedulingService scheduling; @Autowired JdbcTemplate jdbc;
    record Attempt(String result, Long appointmentId) {}

    @Test void concurrentUsersCanReserveTheSameGeneralSlotOnlyOnce() throws Exception {
        String suffix=UUID.randomUUID().toString();
        Long professional=scheduling.createProfessional("Pro","Concurrente","CC","P"+suffix,"pro-"+suffix+"@example.test","300","hash","PC"+suffix,"LIC"+suffix);
        Long owner=jdbc.queryForObject("select user_id from professionals where id=?",Long.class,professional);
        Long general=scheduling.createSpecialty("GEN"+suffix,"General "+suffix,30,true).id();
        Long location=jdbc.queryForObject("select id from locations where active=true limit 1",Long.class);
        scheduling.setProfessionalSpecialties(professional,List.of(general),general);
        scheduling.setProfessionalLocations(professional,List.of(location));
        LocalDate date=LocalDate.now().plusDays(3); LocalDateTime start=LocalDateTime.of(date,LocalTime.of(9,0));
        scheduling.createBlock(owner,location,date,LocalTime.of(9,0),LocalTime.of(10,0));
        Long firstUser=user("concurrent-a-"+suffix+"@example.test","CA"+suffix);
        Long secondUser=user("concurrent-b-"+suffix+"@example.test","CB"+suffix);
        CountDownLatch gate=new CountDownLatch(1);
        ExecutorService pool=Executors.newFixedThreadPool(2);
        try {
            Callable<Attempt> reserveA=attempt(gate,firstUser,professional,location,general,start);
            Callable<Attempt> reserveB=attempt(gate,secondUser,professional,location,general,start);
            Future<Attempt> first=pool.submit(reserveA); Future<Attempt> second=pool.submit(reserveB); gate.countDown();
            List<Attempt> attempts=List.of(first.get(),second.get());
            System.out.println("reservation-attempts="+attempts);
            assertThat(attempts).extracting(Attempt::result).containsExactlyInAnyOrder("APPROVED","409");
            assertThat(attempts.stream().filter(a -> "APPROVED".equals(a.result())).findFirst().orElseThrow().appointmentId()).isNotNull();
            assertThat(jdbc.queryForObject("select count(distinct appointment_id) from professional_slots ps join availability_blocks b on b.id=ps.availability_block_id where b.professional_id=? and ps.start_at=? and ps.appointment_id is not null",Integer.class,professional,start)).isEqualTo(1);
        } finally { pool.shutdownNow(); }
    }

    private Callable<Attempt> attempt(CountDownLatch gate,Long user,Long professional,Long location,Long specialty,LocalDateTime start) {
        return () -> { gate.await(); try { SchedulingService.Appointment appointment=scheduling.reserve(user,professional,location,specialty,start,"Concurrent test"); return new Attempt(appointment.status(),appointment.id()); } catch (SchedulingFailure failure) { return new Attempt("409",null); } };
    }

    @Test void createsProfessionalWithRoleAndRestrictsInactiveAgenda() {
        String suffix=UUID.randomUUID().toString();
        Long professional=scheduling.createProfessional("Profesional","Sintético","CC","P"+suffix,"professional-"+suffix+"@example.test","3000000000","hash","PC"+suffix,"LIC"+suffix);
        Long owner=jdbc.queryForObject("select user_id from professionals where id=?",Long.class,professional);
        assertThat(jdbc.queryForObject("select count(*) from user_roles ur join roles r on r.id=ur.role_id where ur.user_id=? and r.code='PROFESSIONAL'",Integer.class,owner)).isEqualTo(1);
        Long first=scheduling.createSpecialty("A"+suffix,"Especialidad A "+suffix,30,false).id();
        Long second=scheduling.createSpecialty("B"+suffix,"Especialidad B "+suffix,60,false).id();
        List<Long> locations=jdbc.queryForList("select id from locations where active=true order by id limit 2",Long.class);
        scheduling.setProfessionalSpecialties(professional,List.of(first,second),second);
        scheduling.setProfessionalLocations(professional,locations);
        assertThat(jdbc.queryForObject("select count(*) from professional_specialties where professional_id=? and active=true",Integer.class,professional)).isEqualTo(2);
        assertThat(jdbc.queryForObject("select count(*) from professional_specialties where professional_id=? and specialty_id=? and is_primary=true",Integer.class,professional,second)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from professional_locations where professional_id=? and active=true",Integer.class,professional)).isEqualTo(2);
        scheduling.setProfessionalActive(professional,false);
        LocalDate date=LocalDate.now().plusDays(2);
        assertThatThrownBy(() -> scheduling.createBlock(owner,locations.get(0),date,LocalTime.of(9,0),LocalTime.of(10,0))).hasMessageContaining("Profesional");
    }

    @Test void retainsConsecutiveSlotsAndReleasesThemAfterAdministrativeRejection() {
        String suffix=UUID.randomUUID().toString();
        Long professionalUser=user("prof-"+suffix+"@example.test",suffix); Long professional=scheduling.createProfessional("Pro","Fes","CC","P"+suffix,"pro2-"+suffix+"@example.test","300", "hash", "PC"+suffix,"LIC"+suffix);
        // createProfessional creates a second user; use that owner for the professional block
        Long owner=jdbc.queryForObject("select user_id from professionals where id=?",Long.class,professional);
        Long specialty=scheduling.createSpecialty("ESP"+suffix,"Especialidad "+suffix,60,false).id(); Long location=jdbc.queryForObject("select id from locations where active=true limit 1",Long.class);
        scheduling.setProfessionalSpecialties(professional,List.of(specialty),specialty); scheduling.setProfessionalLocations(professional,List.of(location));
        LocalDate date=LocalDate.now().plusDays(2); scheduling.createBlock(owner,location,date,LocalTime.of(8,0),LocalTime.of(10,0));
        assertThat(scheduling.availability(location,specialty,professional,date)).anyMatch(a -> a.startAt().equals(LocalDateTime.of(date,LocalTime.of(8,0))) && a.endAt().equals(LocalDateTime.of(date,LocalTime.of(9,0))));
        Long patient=user("patient-"+suffix+"@example.test","U"+suffix); Long admin=user("admin-"+suffix+"@example.test","A"+suffix);
        SchedulingService.Appointment appointment=scheduling.reserve(patient,professional,location,specialty,LocalDateTime.of(date,LocalTime.of(8,0)),"Prueba");
        assertThat(appointment.status()).isEqualTo("REQUESTED");
        assertThat(jdbc.queryForObject("select count(*) from professional_slots where appointment_id=?", Integer.class, appointment.id())).isEqualTo(2);
        assertThatThrownBy(() -> scheduling.reserve(patient,professional,location,specialty,LocalDateTime.of(date,LocalTime.of(8,0)),"Duplicada")).hasMessageContaining("Franja");
        assertThat(scheduling.decide(admin,appointment.id(),"REJECT","Sin disponibilidad clínica").status()).isEqualTo("REJECTED");
        assertThat(scheduling.availability(location,specialty,professional,date)).anyMatch(a -> a.startAt().equals(LocalDateTime.of(date,LocalTime.of(8,0))));
    }
    private Long user(String email,String document) { jdbc.update("insert into users(first_name,last_name,document_type,document_number,email,phone,password_hash,active,email_verified) values ('Test','User','CC',?,?,?,'hash',true,false)",document,email,"300"); return jdbc.queryForObject("select id from users where email=?",Long.class,email); }
}
