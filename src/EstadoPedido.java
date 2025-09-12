public enum EstadoPedido {
    REGISTRADO,
    EN_PREPARACION,
    LISTO,
    ENTREGADO;

    public boolean puedeTransitarA(EstadoPedido nuevo) {
        return switch (this) {
            case REGISTRADO     -> (nuevo == EN_PREPARACION);
            case EN_PREPARACION -> (nuevo == LISTO);
            case LISTO          -> (nuevo == ENTREGADO);
            case ENTREGADO      -> false;
        };
    }
}

