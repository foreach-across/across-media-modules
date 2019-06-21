package com.foreach.across.modules.filemanager.services;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.AntPathMatcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Arne Vandamme
 * @since 1.4.0
 */
class AntPathMatchingFileVisitor extends SimpleFileVisitor<Path>
{
	private static final Pattern INVALID_PATTERN = Pattern.compile( "/?\\.{1,2}(/|$)" );

	private final AntPathMatcher pathMatcher;
	private final String pattern;
	private final boolean matchOnlyDirectories;
	private final String absolutePathPrefix;
	private final Consumer<Path> matchedPathConsumer;
	private final boolean recursive;

	private int filesVisited;

	private AntPathMatchingFileVisitor( AntPathMatcher pathMatcher,
	                                    String pattern,
	                                    boolean matchOnlyDirectories,
	                                    Path basedir,
	                                    Consumer<Path> matchedPathConsumer ) {
		this.pathMatcher = pathMatcher;
		this.pattern = "/" + pattern;
		this.matchOnlyDirectories = matchOnlyDirectories;
		this.matchedPathConsumer = matchedPathConsumer;

		absolutePathPrefix = StringUtils.replace( basedir.toAbsolutePath().toString(), "\\", "/" );
		recursive = pattern.contains( "**" );
	}

	@Override
	public FileVisitResult preVisitDirectory( Path dir, BasicFileAttributes attrs ) throws IOException {
		filesVisited++;
		String pathToMatch = pathToMatch( dir );

		if ( pathToMatch == null ) {
			return FileVisitResult.CONTINUE;
		}

		boolean exactMatch = false;

		if ( pathMatcher.match( pattern, pathToMatch ) ) {
			exactMatch = true;
			matchedPathConsumer.accept( dir );

			if ( !recursive ) {
				return FileVisitResult.SKIP_SUBTREE;
			}
		}

		if ( exactMatch || pathMatcher.matchStart( pattern, pathToMatch ) ) {
			return FileVisitResult.CONTINUE;
		}

		return FileVisitResult.SKIP_SUBTREE;
	}

	@Override
	public FileVisitResult visitFile( Path file, BasicFileAttributes attrs ) throws IOException {
		filesVisited++;

		if ( !matchOnlyDirectories ) {
			String pathToMatch = pathToMatch( file );
			if ( pathMatcher.match( pattern, pathToMatch ) ) {
				matchedPathConsumer.accept( file );
			}
		}

		/**if ( ( !shouldRecurse || ( usePathMatcher ? pathMatcher.match( antPattern, pathToMatch ) : StringUtils.equals( pathToMatch, antPattern ) ) )
		 && ( !matchOnlyDirectories || candidate.toFile().isDirectory() ) ) {
		 resources.add( toFileRepositoryResource( candidate, pathToMatch ) );
		 }**/

		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed( Path file, IOException exc ) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory( Path dir, IOException exc ) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	private String pathToMatch( Path file ) {
		String candidatePath = StringUtils.replace( file.toAbsolutePath().toString(), "\\", "/" );
		if ( candidatePath.length() > absolutePathPrefix.length() ) {
			return StringUtils.substring( candidatePath, absolutePathPrefix.length() );
		}
		return null;
	}

	@SneakyThrows
	static int walkFileTree( Path directory, String searchPattern, Consumer<Path> matchedPathConsumer ) {
		AntPathMatcher pathMatcher = new AntPathMatcher();

		validatePattern( searchPattern );

		String pattern = StringUtils.removeStart( searchPattern, "/" );

		boolean matchOnlyDirectories = pattern.endsWith( "/" );
		if ( matchOnlyDirectories ) {
			pattern = pattern.substring( 0, pattern.length() - 1 );
		}

		if ( !pathMatcher.isPattern( pattern ) ) {
			Path targetPath = directory.resolve( pattern );
			File file = targetPath.toFile();
			if ( file.exists() && ( !matchOnlyDirectories || file.isDirectory() ) ) {
				matchedPathConsumer.accept( targetPath );
			}
		}
		else {
			String prefix = getFixedPrefix( pattern );
			Path basedir = directory;

			if ( !prefix.isEmpty() ) {
				basedir = basedir.resolve( prefix );
				File file = basedir.toFile();
				if ( !file.exists() || !file.isDirectory() ) {
					return 0;
				}
				pattern = StringUtils.removeStart( pattern, prefix );
			}

			AntPathMatchingFileVisitor visitor = new AntPathMatchingFileVisitor( pathMatcher, pattern, matchOnlyDirectories, basedir, matchedPathConsumer );
			Files.walkFileTree( basedir, visitor );
			return visitor.filesVisited;
		}

		return 0;
	}

	@SuppressWarnings("Duplicates")
	private static String getFixedPrefix( String pattern ) {
		int starIndex = pattern.indexOf( '*' );
		int markIndex = pattern.indexOf( '?' );
		int index = Math.min(
				starIndex == -1 ? pattern.length() : starIndex,
				markIndex == -1 ? pattern.length() : markIndex
		);
		String beforeIndex = pattern.substring( 0, index );
		return beforeIndex.contains( "/" ) ? beforeIndex.substring( 0, beforeIndex.lastIndexOf( '/' ) + 1 ) : "";
	}

	private static void validatePattern( String pattern ) {
		Matcher matcher = INVALID_PATTERN.matcher( pattern );
		if ( matcher.find() ) {
			throw new IllegalArgumentException( "Search pattern may not contain dot-only path segments" );
		}
	}
}
