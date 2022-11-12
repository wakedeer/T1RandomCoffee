package inno.tech.model

import inno.tech.constant.LevelGroup
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

/**
 * Тема встречи.
 */
@Entity
@Table(name = "TOPICS")
data class Topic(

    /** Идентификатор темы */
    @Id
    var id: Long,

    /** Наименование темы */
    @Column(name = "NAME")
    var name: String,

    /** Сложность темы вопросов */
    @Enumerated(EnumType.STRING)
    @Column(name = "LEVEL")
    var level: LevelGroup,

    /** Список вопросов */
    @OneToMany(mappedBy = "topic")
    var questions: Set<Question>,
)
