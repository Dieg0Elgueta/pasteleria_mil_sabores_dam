package org.example
import kotlinx.coroutines.*

data class Cliente(val id: Int, val nombre: String, val email: String, val activo: Boolean){

    fun mostrarCliente(): String {
        return "ID: $id | NOMBRE: $nombre | EMAIL: $email | ESTADO: $activo"
    }
}

data class Pedido(val id: Int, val clienteId: Int, val productos: List<String>, val total: Int, val estado: EstadoPedido){

    fun mostrarPedido(): String {
        return "ID: $id | CLIENTE_ID: $clienteId | PRODUCTOS: $productos | TOTAL: $total | ESTADO: $estado"
    }
}

enum class EstadoPedido {
    PENDIENTE,
    CONFIRMADO,
    ENVIADO,
    ENTREGADO
}

fun manejarBusqueda(estado: EstadoPedido) {
    when (estado) {
        EstadoPedido.PENDIENTE -> println("Tu pedido esta pendiente de confirmacion!")
        EstadoPedido.CONFIRMADO -> println("Tu pedido ha sido confirmado!")
        EstadoPedido.ENVIADO -> println("Tu pedido esta en camino!")
        EstadoPedido.ENTREGADO -> println("Tu pedido ha sido entregado!")
    }
}

sealed class ResultadoOperacion
data class Exitoso(val mensaje: String): ResultadoOperacion()
data class Error(val mensaje: String): ResultadoOperacion()
object Procesando : ResultadoOperacion()

fun manejarResultado(resultado: ResultadoOperacion){
    when(resultado){
        is Exitoso -> println("Operacion Exitosa!")
        is Error -> println("Error!")
        Procesando -> println("Procesando...")
    }
}

class ServicioPedidos(){

    val clientes = listOf(
        Cliente(2, "Juan Perez", "juan@email.com", true),
        Cliente(3, "María Garcia", "maria@email.com", true),
        Cliente(4, "Carlos Lopez", "carlos@email.com", false),
        Cliente(5, "Ana Martínez", "ana@email.com", true)
    )
    suspend fun obtenerCliente(id: Int): Cliente?{
        delay(2000)
        return clientes.find { it.id == id }
    }
}

fun main(): Unit = runBlocking {

    //CREACIÓN CLIENTE, MANEJAR RESULTADOS.
    val cliente1 = Cliente(1, "Diego Elgueta", "dieelgueta@duocuc.cl", true)
    manejarResultado(Procesando)
    println(cliente1.mostrarCliente())
    manejarResultado(Exitoso(""))

    //CREACIÓN PEDIDO, MANEJO DE ESTADOS,
    val pedido1 = Pedido(1, 1,listOf("Laptop", "Mouse", "Teclado"),1500, EstadoPedido.PENDIENTE)
    println(pedido1.mostrarPedido())
    manejarBusqueda(pedido1.estado)

    //USO DE FUNCIONES, OBTENER CLIENTE POR ID.
    val servicio = ServicioPedidos()
    val cliente = servicio.obtenerCliente(2)
    print(cliente)

}
