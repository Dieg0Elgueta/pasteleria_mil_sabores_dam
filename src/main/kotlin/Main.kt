package org.example
import kotlinx.coroutines.*

data class Cliente(val id: Int, val nombre: String, val email: String, val activo: Boolean) {
    fun mostrarCliente(): String {
        return "ID: $id | NOMBRE: $nombre | EMAIL: $email | ESTADO: $activo"
    }
}

data class Pedido(val id: Int, val clienteId: Int, val productos: List<String>, val total: Int, val estado: EstadoPedido) {
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

fun obtenerDescripcionEstado(estado: EstadoPedido): String {
    return when (estado) {
        EstadoPedido.PENDIENTE ->
            "Estado: PENDIENTE - Esperando confirmación del pedido"

        EstadoPedido.CONFIRMADO ->
            "Estado: CONFIRMADO - Pedido confirmado por el sistema"

        EstadoPedido.ENVIADO ->
            "Estado: ENVIADO - Pedido en tránsito hacia el cliente"

        EstadoPedido.ENTREGADO ->
            "Estado: ENTREGADO - Pedido completado exitosamente"
    }
}

// Mostrar descripción del estado usando WHEN
fun mostrarEstado(pedido: Pedido) {
    println("\n" + obtenerDescripcionEstado(pedido.estado))
}

    fun manejarBusqueda(estado: EstadoPedido) {
        when (estado) {
            EstadoPedido.PENDIENTE -> println("Tu pedido este pendiente de confirmacion!")
            EstadoPedido.CONFIRMADO -> println("Tu pedido ha sido confirmado!")
            EstadoPedido.ENVIADO -> println("Tu pedido este en camino!")
            EstadoPedido.ENTREGADO -> println("Tu pedido ha sido entregado!")
        }
    }

    // (se mantiene sealed solo para resultado)
    sealed class ResultadoOperacion
    data class Exitoso(val mensaje: String) : ResultadoOperacion()
    data class Error(val mensaje: String) : ResultadoOperacion()
    object Procesando : ResultadoOperacion()

    fun manejarResultado(resultado: ResultadoOperacion) {
        when (resultado) {
            is Exitoso -> println("Operacion Exitosa!")
            is Error -> println("Error!")
            Procesando -> println("Procesando...")
        }
    }

    class ServicioPedidos {
        val clientes = listOf(
            Cliente(2, "Juan Perez", "juan@email.com", true),
            Cliente(3, "María Garcia", "maria@email.com", true),
            Cliente(4, "Carlos Lopez", "carlos@email.com", false),
            Cliente(5, "Ana Martínez", "ana@email.com", true)
        )

        // FUNCIÓN 1: Obtener cliente
        suspend fun obtenerCliente(id: Int): Cliente? {
            delay(1000)
            return clientes.find { it.id == id }
        }

        // FUNCIÓN 2: Calcular total (con "Martillo")
        suspend fun calcularTotal(productos: List<String>): Int {
            delay(500)
            var total = 0
            for (p in productos) {
                when (p.lowercase()) {
                    "martillo" -> total += 1_000_000
                    "taladro" -> total += 15_000
                    "clavos" -> total += 25_000
                    "tornillos" -> total += 180_000
                    else -> println("Producto '$p' no encontrado en catálogo.")
                }
            }
            println("El total del pedido es: $$total")
            return total
        }

        // esta es la funcion para validar el notebook
        suspend fun validarInventario(productos: List<String>): Boolean {
            delay(500)
            val inventario = mapOf(
                "martillo" to 5,
                "taladro" to 10,
                "clavos" to 7,
                "tornillos" to 3
            )
            var todoDisponible = true

            for (p in productos) {
                val stock = inventario[p.lowercase()] ?: 0
                if (stock > 0) {
                    println("$p disponible (stock: $stock)")
                } else {
                    println("$p sin stock")
                    todoDisponible = false
                }
            }

            if (todoDisponible) {
                println("Inventario validado correctamente.")
            } else {
                println("No hay stock suficiente para completar el pedido.")
            }
            return todoDisponible
        }

        // este guarda el pedido
        suspend fun guardarPedido(pedido: Pedido) {
            delay(1000)
            println("Guardando pedido en sistema...")
            println("Pedido #${pedido.id} guardado correctamente con estado ${pedido.estado}.")
        }
    }

    fun main(): Unit = runBlocking {

        val servicio = ServicioPedidos()

        // ===== Validación de cliente con 'let' =====
        val clienteId = 5  // <-- cambia este ID para probar (2, 3, 4, 5)
        val cliente = servicio.obtenerCliente(clienteId)

        cliente?.let { c ->
            println("Cliente obtenido: ${c.mostrarCliente()}")

            if (!c.activo) {
                println("El cliente está inactivo. No se puede crear el pedido.")
                manejarResultado(Error("Cliente inactivo."))
                return@let
            }

            val productos = listOf("martillo", "taladro", "clavos")
            val total = servicio.calcularTotal(productos)


            val pedidoPendiente = Pedido(
                id = 1001,
                clienteId = c.id,
                productos = productos,
                total = total,
                estado = EstadoPedido.PENDIENTE
            ).apply {
                println("Creando pedido con apply -> clienteId=$clienteId, total=$total")
            }

            println(pedidoPendiente.mostrarPedido())
            mostrarEstado(pedidoPendiente)
            manejarBusqueda(pedidoPendiente.estado)

            val inventarioOk = servicio.validarInventario(pedidoPendiente.productos)
            if (inventarioOk) {
                // Como 'estado' es val, generamos un nuevo pedido ya confirmado
                val pedidoConfirmado = Pedido(
                    id = pedidoPendiente.id,
                    clienteId = pedidoPendiente.clienteId,
                    productos = pedidoPendiente.productos,
                    total = pedidoPendiente.total,
                    estado = EstadoPedido.CONFIRMADO
                )
                manejarBusqueda(pedidoConfirmado.estado)
                servicio.guardarPedido(pedidoConfirmado)
                manejarResultado(Exitoso("Pedido confirmado y guardado."))

            } else {
                manejarResultado(Error("No hay stock suficiente para el pedido."))
            }

        } ?: run {
            println("Cliente no encontrado (id=$clienteId).")
            manejarResultado(Error("Cliente no encontrado."))
        }
    }
