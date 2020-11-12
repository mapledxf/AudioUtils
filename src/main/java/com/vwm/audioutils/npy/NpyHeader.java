package com.vwm.audioutils.npy;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author Xuefeng Ding
 * Created 2020/11/9
 */
public class NpyHeader {
	private String descr;
	private boolean isFortranOrder;
	private DataType dataType;
	private Endian endian;
	private int[] shape;

	public static int[] getShape(String shapeString) {
		String tmp = shapeString;
		tmp = tmp.replace("(", "");
		tmp = tmp.replace(" ", "");
		tmp = tmp.replace(")", "");
		
		String[] arr = tmp.split(",");
		
		int[] result = new int[arr.length];
		for (int i = 0; i < arr.length; i++) {
			result[i] = Integer.parseInt(arr[i]);
		}
		
		return result;
	}
	
	public static NpyHeader getNpyHeader(byte[] data) {
		String str = new String(data);
		str = str.replaceAll(", \\}", "\\}");
		str = str.replace(": (", ": '(");
		str = str.replace(")}", ")'}");
		
		JsonElement element = JsonParser.parseString(str);
		
		JsonObject headerInfo = element.getAsJsonObject();
		
		boolean isFortranOrder = headerInfo.get("fortran_order").getAsBoolean();
		String shapeString = headerInfo.get("shape").getAsString();

		return new NpyHeader(headerInfo.get("descr").getAsString(), isFortranOrder, getShape(shapeString));
	}
	
	public NpyHeader(String descr, boolean isFortranOrder, int[] shape) {
		this.descr = descr;
		this.isFortranOrder = isFortranOrder;
		this.shape = shape.clone();
		
		String endianString = descr.substring(0, 1);
		String typeString = descr.substring(1);
		
		for (Endian ed : Endian.values()) {
			if (ed.toString().equals(endianString)) {
				this.endian = ed;
				break;
			}
		}
		
		for (DataType dt : DataType.values()) {
			if (dt.toString().equals(typeString)) {
				this.dataType = dt;
				break;
			}
		}
	}

	public String getDescription() {
		return this.descr;
	}

	public boolean isFortranOrder() {
		return this.isFortranOrder;
	}

	public DataType getDataType() {
		return this.dataType;
	}

	public Endian getEndian() {
		return this.endian;
	}

	public int[] getShape() {
		return this.shape;
	}
}