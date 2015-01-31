package com.stbam.rssnewsreader.image;

import java.io.InputStream;
import java.io.OutputStream;

// El codigo a continuacion fue tomado de https://github.com/thest1/LazyList/tree/master/src/com/fedorvlasov/lazylist
// Basicamente el contenido de la carpeta image es solomante para poder cargar
// los thumbnails de cada una de las noticias en el Main activity

public class Utils {
	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}
}