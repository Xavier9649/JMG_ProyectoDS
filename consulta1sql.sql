DROP TABLE IF EXISTS detalle_orden;
DROP TABLE IF EXISTS ordenes;
DROP TABLE IF EXISTS platillos;
DROP TABLE IF EXISTS mesas;
DROP TABLE IF EXISTS clientes;
CREATE TABLE cliente (
  id_cliente INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  telefono VARCHAR(30),
  correo VARCHAR(120) UNIQUE
);

CREATE TABLE usuario (
  id_usuario INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  rol ENUM('MESERO','COCINERO') NOT NULL
);

CREATE TABLE menu_item (
  id_item INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(120) NOT NULL,
  precio DECIMAL(10,2) NOT NULL,
  categoria VARCHAR(60)
);

CREATE TABLE pedido (
  id_pedido INT AUTO_INCREMENT PRIMARY KEY,
  id_cliente INT NOT NULL,
  id_mesero INT NOT NULL,
  estado ENUM('pendiente','listo') NOT NULL DEFAULT 'pendiente',
  numero_mesa VARCHAR(10) NULL,
  fecha_creacion DATETIME NOT NULL DEFAULT NOW(),
  CONSTRAINT fk_pedido_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
  CONSTRAINT fk_pedido_mesero  FOREIGN KEY (id_mesero)  REFERENCES usuario(id_usuario),
  INDEX idx_pedido_estado (estado),
  INDEX idx_pedido_fecha (fecha_creacion)
);

CREATE TABLE detalle_pedido (
  id_detalle INT AUTO_INCREMENT PRIMARY KEY,
  id_pedido INT NOT NULL,
  id_item INT NOT NULL,
  cantidad INT NOT NULL,
  precio_unitario DECIMAL(10,2) NOT NULL,
  CONSTRAINT fk_detalle_pedido FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido),
  CONSTRAINT fk_detalle_item   FOREIGN KEY (id_item)   REFERENCES menu_item(id_item),
  INDEX idx_detalle_pedido (id_pedido)
);

CREATE TABLE notificacion (
  id_notificacion INT AUTO_INCREMENT PRIMARY KEY,
  id_pedido INT NOT NULL,
  id_destinatario INT NOT NULL,
  canal ENUM('socket','email') NOT NULL,
  mensaje TEXT,
  visto BOOLEAN NOT NULL DEFAULT 0,
  fecha DATETIME NOT NULL DEFAULT NOW(),
  CONSTRAINT fk_notif_pedido FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido),
  CONSTRAINT fk_notif_usuario FOREIGN KEY (id_destinatario) REFERENCES usuario(id_usuario),
  INDEX idx_notif_visto (visto),
  INDEX idx_notif_fecha (fecha)
);

-- USUARIO: un mesero
INSERT INTO usuario (nombre, rol) VALUES ('Pedro', 'MESERO');
-- Guarda el ID que te devuelve (ej. 1)

-- MENÚ: algunos ítems
INSERT INTO menu_item (nombre, precio, categoria) VALUES
('Café', 2.50, 'Bebida'),
('Empanada', 1.75, 'Snack'),
('Jugo de Naranja', 2.25, 'Bebida');

ALTER TABLE pedido ADD COLUMN total DECIMAL(10,2);
