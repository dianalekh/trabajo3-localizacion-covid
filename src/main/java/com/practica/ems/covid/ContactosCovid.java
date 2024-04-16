package com.practica.ems.covid;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.practica.excecption.EmsDuplicateLocationException;
import com.practica.excecption.EmsDuplicatePersonException;
import com.practica.excecption.EmsInvalidNumberOfDataException;
import com.practica.excecption.EmsInvalidTypeException;
import com.practica.excecption.EmsLocalizationNotFoundException;
import com.practica.excecption.EmsPersonNotFoundException;
import com.practica.genericas.Constantes;
import com.practica.genericas.Coordenada;
import com.practica.genericas.FechaHora;
import com.practica.genericas.Persona;
import com.practica.genericas.PosicionPersona;
import com.practica.lista.ListaContactos;

public class ContactosCovid {
	private Poblacion poblacion;
	private Localizacion localizacion;
	private ListaContactos listaContactos;

	private Persona persona;

	public ContactosCovid() {
		this.poblacion = new Poblacion();
		this.localizacion = new Localizacion();
		this.listaContactos = new ListaContactos();
		this.persona = new Persona();
	}

	public Poblacion getPoblacion() {
		return poblacion;
	}

	public Localizacion getLocalizacion() {
		return localizacion;
	}

	public void setLocalizacion(Localizacion localizacion) {
		this.localizacion = localizacion;
	}

	public ListaContactos getListaContactos() {
		return listaContactos;
	}


	public void loadData(String data, boolean reset) throws EmsInvalidTypeException, EmsInvalidNumberOfDataException,
			EmsDuplicatePersonException, EmsDuplicateLocationException {
		// borro información anterior
		if (reset) {
			this.poblacion = new Poblacion();
			this.localizacion = new Localizacion();
			this.listaContactos = new ListaContactos();
		}
		String datas[] = dividirEntrada(data);
		for (String linea : datas) {
			String datos[] = this.dividirLineaData(linea);
			if (!datos[0].equals("PERSONA") && !datos[0].equals("LOCALIZACION")) {
				throw new EmsInvalidTypeException();
			}
			if (datos[0].equals("PERSONA")) {
				if (datos.length != Constantes.MAX_DATOS_PERSONA) {
					throw new EmsInvalidNumberOfDataException("El número de datos para PERSONA es menor de 8");
				}
				this.poblacion.addPersona(persona.crearPersona(datos));
			}
			if (datos[0].equals("LOCALIZACION")) {
				if (datos.length != Constantes.MAX_DATOS_LOCALIZACION) {
					throw new EmsInvalidNumberOfDataException("El número de datos para LOCALIZACION es menor de 6");
				}
				PosicionPersona pp = this.crearPosicionPersona(datos);
				this.localizacion.addLocalizacion(pp);
				this.listaContactos.insertarNodoTemporal(pp);
			}
		}
	}

	public void loadDataFile(String fichero, boolean reset) {
		FileReader fr = null;
		String datas[] = null, data = null;
		loadDataFile(fichero, reset, fr, datas, data);
		
	}

	private void resetBufferReader(boolean reset){
		if (reset) {
			this.poblacion = new Poblacion();
			this.localizacion = new Localizacion();
			this.listaContactos = new ListaContactos();
		}
	}

	private void niPersonaniLocalización (String datos[])throws EmsInvalidTypeException{
		if (!datos[0].equals("PERSONA") && !datos[0].equals("LOCALIZACION")) {
			throw new EmsInvalidTypeException();
		}
	}

	private void esPersona(String datos[]) throws EmsInvalidNumberOfDataException, EmsDuplicatePersonException {
		if (datos[0].equals("PERSONA")) {
			numDatosPersona(datos);
			this.poblacion.addPersona(persona.crearPersona(datos));
		}
	}

	private void esLocalizacion(String datos[]) throws EmsInvalidNumberOfDataException, EmsDuplicateLocationException {
		if (datos[0].equals("LOCALIZACION")) {
			numeDatosLocalizacion(datos);
			PosicionPersona pp = this.crearPosicionPersona(datos);
			this.localizacion.addLocalizacion(pp);
			this.listaContactos.insertarNodoTemporal(pp);
		}
	}

	private void numDatosPersona(String datos[])throws EmsInvalidNumberOfDataException{
		if (datos.length != Constantes.MAX_DATOS_PERSONA) {
			throw new EmsInvalidNumberOfDataException("El número de datos para PERSONA es menor de 8");
		}
	}

	private void numeDatosLocalizacion(String datos[])throws EmsInvalidNumberOfDataException{
		if (datos.length != Constantes.MAX_DATOS_LOCALIZACION) {
			throw new EmsInvalidNumberOfDataException("El número de datos para LOCALIZACION es menor de 6" );
		}
	}

	private void cambioLinea(String[] datas) throws EmsInvalidTypeException, EmsInvalidNumberOfDataException, EmsDuplicatePersonException, EmsDuplicateLocationException {
		for (String linea : datas) {
			String datos[] = this.dividirLineaData(linea);
			niPersonaniLocalización(datos);
			esPersona(datos);
			esLocalizacion(datos);
		}
	}

	private void fileReaderVacio(FileReader fr)throws IOException {
		if (null != fr) {
			fr.close();
		}
	}

	private void dividirEntrada(String[] datas,String data, BufferedReader br) throws IOException, EmsInvalidNumberOfDataException, EmsDuplicateLocationException, EmsInvalidTypeException, EmsDuplicatePersonException {
		while ((data = br.readLine()) != null) {
			datas = dividirEntrada(data.trim());
			cambioLinea(datas);
		}
	}
	@SuppressWarnings("resource")
	public void loadDataFile(String fichero, boolean reset, FileReader fr, String datas[], String data ) {
		try {
			// Apertura del fichero y creacion de BufferedReader para poder
			// hacer una lectura comoda (disponer del metodo readLine()).
			File archivo = new File(fichero);
			fr = new FileReader(archivo);
			BufferedReader br = new BufferedReader(fr);
			resetBufferReader(reset);
			/**
			 * Lectura del fichero	línea a línea. Compruebo que cada línea 
			 * tiene el tipo PERSONA o LOCALIZACION y cargo la línea de datos en la 
			 * lista correspondiente. Sino viene ninguno de esos tipos lanzo una excepción
			 */
			dividirEntrada(datas,data,br);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// En el finally cerramos el fichero, para asegurarnos
			// que se cierra tanto si todo va bien como si salta
			// una excepcion.
			try {
				fileReaderVacio(fr);
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
	public int findPersona(String documento) throws EmsPersonNotFoundException {
		int pos;
		try {
			pos = this.poblacion.findPersona(documento);
			return pos;
		} catch (EmsPersonNotFoundException e) {
			throw new EmsPersonNotFoundException();
		}
	}

	public int findLocalizacion(String documento, String fecha, String hora) throws EmsLocalizationNotFoundException {

		int pos;
		try {
			pos = localizacion.findLocalizacion(documento, fecha, hora);
			return pos;
		} catch (EmsLocalizationNotFoundException e) {
			throw new EmsLocalizationNotFoundException();
		}
	}

	public List<PosicionPersona> localizacionPersona(String documento) throws EmsPersonNotFoundException {
		int cont = 0;
		List<PosicionPersona> lista = new ArrayList<PosicionPersona>();
		Iterator<PosicionPersona> it = this.localizacion.getLista().iterator();
		while (it.hasNext()) {
			PosicionPersona pp = it.next();
			if (pp.getDocumento().equals(documento)) {
				cont++;
				lista.add(pp);
			}
		}
		if (cont == 0)
			throw new EmsPersonNotFoundException();
		else
			return lista;
	}

	public boolean delPersona(String documento) throws EmsPersonNotFoundException {
		int cont = 0, pos = -1;
		Iterator<Persona> it = this.poblacion.getLista().iterator();
		while (it.hasNext()) {
			Persona persona = it.next();
			if (persona.getDocumento().equals(documento)) {
				pos = cont;
			}
			cont++;
		}
		if (pos == -1) {
			throw new EmsPersonNotFoundException();
		}
		this.poblacion.getLista().remove(pos);
		return false;
	}

	private String[] dividirEntrada(String input) {
		String cadenas[] = input.split("\\n");
		return cadenas;
	}

	private String[] dividirLineaData(String data) {
		String cadenas[] = data.split("\\;");
		return cadenas;
	}

	private PosicionPersona crearPosicionPersona(String[] data) {
		PosicionPersona posicionPersona = new PosicionPersona();
		String fecha = null, hora;
		float latitud = 0, longitud;

		// Verificar que el array de datos tenga la longitud adecuada
		if (data.length >= Constantes.MAX_DATOS_LOCALIZACION) {
			posicionPersona.setDocumento(data[1]);
			fecha = data[2];
			hora = data[3];
			posicionPersona.setFechaPosicion(FechaHora.parsearFecha(fecha, hora));
			latitud = Float.parseFloat(data[4]);
			longitud = Float.parseFloat(data[5]);
			posicionPersona.setCoordenada(new Coordenada(latitud, longitud));
		} else {
			// Manejar caso en el que el array de datos no tiene la longitud adecuada
			System.out.println("El array de datos no tiene la longitud adecuada.");
		}

		return posicionPersona;
	}
}
