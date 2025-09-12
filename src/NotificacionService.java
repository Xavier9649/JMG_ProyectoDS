public class NotificacionService {

        public void notificarPedidoListo(Pedido pedido) {
            if (pedido == null || pedido.getId() == null)
                throw new IllegalArgumentException("Pedido inválido");
            System.out.println("[NOTIFICACIÓN] Pedido #" + pedido.getId() + " está LISTO para el mesero.");
        }
}

