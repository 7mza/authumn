package com.authumn.authumn.commons

interface ICrud<T, U, V> {
    fun save(t: T): V

    fun saveMany(t: Collection<T>): Collection<V>

    fun findById(id: String): V

    fun findManyByIds(ids: Collection<String>?): Collection<V>

    fun findAll(): Collection<V>

    fun update(
        id: String,
        u: U,
    ): V

    fun deleteById(id: String)

    fun deleteAll()

    fun count(): Long

    fun existsById(id: String): Boolean
}
