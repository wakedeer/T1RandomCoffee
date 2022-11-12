package inno.tech.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

/**
 * Вопрос.
 */
@Entity
@Table(name = "QUESTIONS")
data class Question(

    /** Идентификатор вопроса */
    @Id
    @Column(name = "ID")
    val id: Long,

    /** Идентификатор темы вопросов */
    @ManyToOne
    @JoinColumn(name = "TOPIC_ID", nullable = false)
    val topic: Topic,

    /** Текст вопроса */
    @Column(name = "CONTENT")
    val content: String,
)
