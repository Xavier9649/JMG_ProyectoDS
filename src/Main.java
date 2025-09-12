

import java.awt.*;
import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) {
        // 1) Crear/guardar cliente
        Cliente cliente = new Cliente("Juan Pérez", "juan@example.com", "0999999999");
        cliente.guardar();

        // 2) Crear/guardar items de menú
        MenuItem cafe   = new MenuItem("Café Americano", new BigDecimal("1.50"));
        MenuItem sandwich = new MenuItem("Sándwich de pollo", new BigDecimal("3.75"));
        cafe.guardar();
        sandwich.guardar();

        // 3) Crear pedido + detalles
        Pedido pedido = new Pedido(cliente);
        pedido.agregarDetalle(cafe, 2);       // 2 cafés
        pedido.agregarDetalle(sandwich, 1);   // 1 sándwich
        pedido.guardar();

        // 4) Flujo de estados
        pedido.actualizarEstado(EstadoPedido.EN_PREPARACION);
        pedido.actualizarEstado(EstadoPedido.LISTO);

        // 5) Notificar
        NotificacionService noti = new NotificacionService();
        noti.notificarPedidoListo(pedido);
    }
}
