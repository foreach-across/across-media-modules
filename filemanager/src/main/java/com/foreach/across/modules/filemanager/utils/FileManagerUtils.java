package com.foreach.across.modules.filemanager.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileManagerUtils
{
	public static void fastCopy( InputStream original, File target ) throws IOException {
		try (FileChannel output = FileChannel.open( Paths.get( target.toURI() ), StandardOpenOption.WRITE )) {
			try (ReadableByteChannel input = Channels.newChannel( original )) {
				channelCopy( input, output );
			}
		}
	}

	public static void fastCopy( File original, File target ) throws IOException {
		try (FileChannel output = FileChannel.open( Paths.get( target.toURI() ), StandardOpenOption.WRITE )) {
			try (ReadableByteChannel input = FileChannel.open( Paths.get( original.toURI() ),
			                                                   StandardOpenOption.READ )) {
				channelCopy( input, output );
			}
		}
	}

	public static void channelCopy( ReadableByteChannel src, WritableByteChannel dest ) throws IOException {
		final ByteBuffer buffer = ByteBuffer.allocateDirect( 16 * 1024 );
		while ( src.read( buffer ) != -1 ) {
			// prepare the buffer to be drained
			buffer.flip();
			// write to the channel, may block
			dest.write( buffer );
			// If partial transfer, shift remainder down
			// If buffer is empty, same as doing clear()
			buffer.compact();
		}
		// EOF will leave buffer in fill state
		buffer.flip();
		// make sure the buffer is fully drained.
		while ( buffer.hasRemaining() ) {
			dest.write( buffer );
		}
	}
}
