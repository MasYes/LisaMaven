package ru.lisaprog;

/**
 * Created with IntelliJ IDEA.
 * User: masyes
 * Date: 03.08.13
 * Time: 16:57
 * To change this template use File | Settings | File Templates.
 */
public class UnsupportedFormatException extends RuntimeException {
	public UnsupportedFormatException() {
		super();
	}

	public UnsupportedFormatException(String s) {
		super(s);
	}

	public UnsupportedFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedFormatException(Throwable cause) {
		super(cause);
	}
}
