package inno.tech.constant

/**
 * Группы уровней знания языка. Используются для тем вопросов.
 */
enum class LevelGroup(vararg subLevels: Level) {
    A(Level.A1, Level.A2),
    B(Level.B1, Level.B2),
    C(Level.C1, Level.C2),
}
