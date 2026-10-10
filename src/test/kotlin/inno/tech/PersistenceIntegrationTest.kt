package inno.tech

import inno.tech.constant.MeetingFormat
import inno.tech.constant.Status
import inno.tech.model.Meeting
import inno.tech.model.User
import inno.tech.repository.MeetingRepository
import inno.tech.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.time.LocalDateTime

/**
 * Проверяет запуск приложения, миграции Liquibase и JPA-репозитории на PostgreSQL 18.6.
 */
@Testcontainers
@SpringBootTest(
    properties = [
        "telegram.token=test-token",
        "spring.autoconfigure.exclude=org.telegram.telegrambots.longpolling.starter.TelegramBotStarterConfiguration",
        "spring.task.scheduling.enabled=false",
    ],
)
@Transactional
internal class PersistenceIntegrationTest {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var meetingRepository: MeetingRepository

    @Test
    fun `liquibase creates the schema expected by hibernate`() {
        val executedChanges = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM databasechangelog",
            Long::class.javaObjectType,
        )
        val applicationTables = jdbcTemplate.queryForList(
            """
            SELECT table_name
            FROM information_schema.tables
            WHERE table_schema = 'public'
              AND table_name IN ('users', 'meetings')
            """.trimIndent(),
            String::class.java,
        )
        val meetingIdType = jdbcTemplate.queryForObject(
            """
            SELECT data_type
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'meetings'
              AND column_name = 'id'
            """.trimIndent(),
            String::class.java,
        )

        assertThat(executedChanges).isEqualTo(4)
        assertThat(applicationTables).containsExactlyInAnyOrder("users", "meetings")
        assertThat(meetingIdType).isEqualTo("bigint")
    }

    @Test
    fun `user repository filters active users by status format and city`() {
        userRepository.saveAll(
            listOf(
                user(id = 1, status = Status.READY, meetingFormat = MeetingFormat.OFFLINE),
                user(id = 2, status = Status.READY, meetingFormat = MeetingFormat.ONLINE),
                user(id = 3, status = Status.MATCHED, meetingFormat = MeetingFormat.OFFLINE),
                user(id = 4, status = Status.READY, meetingFormat = MeetingFormat.OFFLINE, active = false),
            ),
        )

        val readyUsers = userRepository.findAllByStatusAndActiveTrue(Status.READY)
        val offlineMoscowUsers = userRepository.findAllByStatusAndMeetingFormatAndCityAndActiveTrue(
            status = Status.READY,
            meetingFormat = MeetingFormat.OFFLINE,
            city = TEST_CITY,
        )
        val invitedUsers = userRepository.findAllByStatusInAndActiveTrue(listOf(Status.READY, Status.MATCHED))

        assertThat(readyUsers.map(User::userId)).containsExactlyInAnyOrder(1, 2)
        assertThat(offlineMoscowUsers.map(User::userId)).containsExactly(1)
        assertThat(invitedUsers.map(User::userId)).containsExactlyInAnyOrder(1, 2, 3)
    }

    @Test
    fun `meeting repository stores bigint identity and finds recent pair in either order`() {
        val matchDate = LocalDateTime.now().minusDays(2)
        val meeting = meetingRepository.save(
            Meeting(
                userId1 = 10,
                userId2 = 20,
                matchDate = matchDate,
                meetingFormat = MeetingFormat.ONLINE,
            ),
        )

        assertThat(meeting.id).isPositive()
        assertThat(meetingRepository.existsMeetingAfter(10, 20, matchDate.minusMinutes(1))).isTrue()
        assertThat(meetingRepository.existsMeetingAfter(20, 10, matchDate.minusMinutes(1))).isTrue()
        assertThat(meetingRepository.existsMeetingAfter(10, 20, matchDate.plusMinutes(1))).isFalse()
    }

    private fun user(
        id: Long,
        status: Status,
        meetingFormat: MeetingFormat,
        active: Boolean = true,
    ) = User(
        userId = id,
        chatId = id * 10,
        fullName = "User $id",
        city = TEST_CITY,
        status = status,
        active = active,
        meetingFormat = meetingFormat,
    )

    companion object {

        private const val TEST_CITY = "Москва"

        @Container
        @JvmField
        val postgres = PostgreSQLContainer("postgres:18.6")
            .withDatabaseName("random_coffee_test")
            .withUsername("random")
            .withPassword("coffee-test")

        /**
         * Передаёт Spring Boot параметры подключения к временному PostgreSQL-контейнеру.
         *
         * @param registry реестр динамических свойств тестового контекста
         */
        @DynamicPropertySource
        @JvmStatic
        fun registerDataSourceProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }
}
