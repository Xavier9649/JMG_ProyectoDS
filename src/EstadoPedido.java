public enum EstadoPedido {
    PENDIENTE,
    EN_PREPARACION,
    LISTO;

    public boolean puedeTransitarA(EstadoPedido nuevo) {
        return switch (this) {
            case PENDIENTE -> nuevo == EN_PREPARACION || nuevo == LISTO;
            case EN_PREPARACION -> nuevo == LISTO;
            case LISTO -> false;
        };
    }

    public String toDb() {
        return switch (this) {
            case PENDIENTE -> "pendiente";
            case EN_PREPARACION -> "en_preparacion";
            case LISTO -> "listo";
        };
    }

    public static EstadoPedido fromDb(String s) {
        return switch (s.toLowerCase()) {
            case "en_preparacion" -> EN_PREPARACION;
            case "listo" -> LISTO;
            default -> PENDIENTE;
        };
    }
}

