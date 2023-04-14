package com.alura.jdbc.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alura.jdbc.factory.ConnectionFactory;

public class ProductoController {

	public int modificar(String nombre, String descripcion, Integer cantidad, Integer id) throws SQLException {
		ConnectionFactory factory = new ConnectionFactory();
		final Connection con = factory.recuperaConexion();
		try (con) {
			final PreparedStatement statement = con.prepareStatement("UPDATE PRODUCTO SET " + " NOMBRE = ?"
					+ ", DESCRIPCION = ? " + ", CANTIDAD = ? " + " WHERE ID = ?");
			try (statement) {
				statement.setString(1, nombre);
				statement.setString(2, descripcion);
				statement.setInt(3, cantidad);
				statement.setInt(4, id);
				statement.execute();

				int updateCount = statement.getUpdateCount();

				return updateCount;
			}
		}
	}

	public int eliminar(Integer id) throws SQLException {
		ConnectionFactory factory = new ConnectionFactory();
		final Connection con = factory.recuperaConexion();

		try (con) {
			final PreparedStatement statement = con.prepareStatement("DELETE FROM PRODUCTO WHERE ID = ?");
			try (statement) {
				statement.setInt(1, id);
				statement.execute();

				int updateCount = statement.getUpdateCount();

				return updateCount; // RETORNA CUANTOS PRODUCTOS FUERON ELIMINADOS
			}
		}
	}

	public List<Map<String, String>> listar() throws SQLException {
		ConnectionFactory factory = new ConnectionFactory();
		final Connection con = factory.recuperaConexion(); // Llamar a los métodos crea y recupera conexión.

		try (con) {
			final PreparedStatement statement = con
					.prepareStatement("SELECT ID, NOMBRE, DESCRIPCION, CANTIDAD FROM PRODUCTO");

			try (statement) {
				statement.execute();

				ResultSet resultSet = statement.getResultSet();

				List<Map<String, String>> resultado = new ArrayList<>();

				while (resultSet.next()) {
					Map<String, String> fila = new HashMap<>();
					fila.put("ID", String.valueOf(resultSet.getInt("ID")));
					fila.put("NOMBRE", resultSet.getString("NOMBRE"));
					fila.put("DESCRIPCION", resultSet.getString("DESCRIPCION"));
					fila.put("CANTIDAD", String.valueOf(resultSet.getInt("CANTIDAD")));

					resultado.add(fila);
				}

				return resultado;
			}
		}
	}

	public void guardar(Map<String, String> producto) throws SQLException {
		String nombre = producto.get("NOMBRE");
		String descripcion = producto.get("DESCRIPCION");
		Integer cantidad = Integer.valueOf(producto.get("CANTIDAD"));
		Integer maximoCantidad = 50;

		ConnectionFactory factory = new ConnectionFactory();
		final Connection con = factory.recuperaConexion();

		try (con) {
			con.setAutoCommit(false); // setea la conexion y otorga el control manual de transacción al
										// desarrollador(error: con esto, no aparecen los registros pantalla)

			final PreparedStatement statement = con.prepareStatement(
					"INSERT INTO PRODUCTO " + "(nombre, descripcion, cantidad)" + " VALUES(?,?,?)",
					Statement.RETURN_GENERATED_KEYS);

			try (statement) {
				do { // Control de cantidades: con esta logica, si supera los 50 registros, se divide
						// en 2
					int cantidadParaGuardar = Math.min(cantidad, maximoCantidad); // maximoCantidad es 50 en este
																					// caso
					ejecutaRegistro(nombre, descripcion, cantidadParaGuardar, statement);
					cantidad -= maximoCantidad; // mientras el valor ingresado sea menor al valor maximo, continua
												// el loop
				} while (cantidad > 0);

				con.commit();

			} catch (Exception e) {
				con.rollback();
			}
		}
	}

	private void ejecutaRegistro(String nombre, String descripcion, Integer cantidad, PreparedStatement statement)
			throws SQLException {
		statement.setString(1, nombre);
		statement.setString(2, descripcion);
		statement.setInt(3, cantidad);

		statement.execute();

		final ResultSet resultSet = statement.getGeneratedKeys(); // Declaramos como final

		try (resultSet) { // Encapsulamos el resultSet en un try
			while (resultSet.next()) {
				System.out.println(String.format("Fué insertado el producto con ID %d", resultSet.getInt(1)));
			}
		}
	}

}
