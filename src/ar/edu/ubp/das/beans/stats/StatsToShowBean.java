package ar.edu.ubp.das.beans.stats;

public class StatsToShowBean {
	private int popularidad;
	private int fecha;
	private int ascendente;
	private int descendente;
	private int sinResultados;
	private int conResultados;
	private int realizadasHoy;
	private int totales;

	public int getPopularidad() {
		return popularidad;
	}

	public void setPopularidad(int popularidad) {
		this.popularidad = popularidad;
	}

	public int getFecha() {
		return fecha;
	}

	public void setFecha(int fecha) {
		this.fecha = fecha;
	}

	public int getAscendente() {
		return ascendente;
	}

	public void setAscendente(int ascendente) {
		this.ascendente = ascendente;
	}

	public int getDescendente() {
		return descendente;
	}

	public void setDescendente(int descendente) {
		this.descendente = descendente;
	}

	public int getSinResultados() {
		return sinResultados;
	}

	public void setSinResultados(int sinResultados) {
		this.sinResultados = sinResultados;
	}

	public int getConResultados() {
		return conResultados;
	}

	public void setConResultados(int conResultados) {
		this.conResultados = conResultados;
	}

	public int getRealizadasHoy() {
		return realizadasHoy;
	}

	public void setRealizadasHoy(int realizadasHoy) {
		this.realizadasHoy = realizadasHoy;
	}

	public int getTotales() {
		return totales;
	}

	public void setTotales(int totales) {
		this.totales = totales;
	}
}
