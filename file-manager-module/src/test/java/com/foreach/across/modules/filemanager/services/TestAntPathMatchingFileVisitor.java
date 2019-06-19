package com.foreach.across.modules.filemanager.services;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author Arne Vandamme
 * @since 1.4.0
 */
class TestAntPathMatchingFileVisitor
{
	@SuppressWarnings("WeakerAccess")
	@TempDir
	static File tempDir;

	private static File childFolder;
	private static File childFile;
	private static File childFileInChildFolder;
	private static File childFolderInChildFolder;
	private static File otherChildFolderInChildFolder;
	private static File childFileInChildFolderInChildFolder;
	private static File childFileInOtherChildFolderInChildFolder;

	@BeforeAll
	@SneakyThrows
	static void createFiles() {
		childFolder = new File( tempDir, "childFolder" );
		assertThat( childFolder.mkdir() ).isTrue();

		childFile = new File( tempDir, "childFile.txt" );
		assertThat( childFile.createNewFile() ).isTrue();

		childFileInChildFolder = new File( childFolder, "childFileInChildFolder.txt" );
		assertThat( childFileInChildFolder.createNewFile() ).isTrue();

		childFolderInChildFolder = new File( childFolder, "childFolderInChildFolder" );
		assertThat( childFolderInChildFolder.mkdir() ).isTrue();

		otherChildFolderInChildFolder = new File( childFolder, "otherChildFolderInChildFolder" );
		assertThat( otherChildFolderInChildFolder.mkdir() ).isTrue();

		childFileInChildFolderInChildFolder = new File( childFolderInChildFolder, "childFileInChildFolderInChildFolder.txt" );
		assertThat( childFileInChildFolderInChildFolder.createNewFile() ).isTrue();

		childFileInOtherChildFolderInChildFolder = new File( otherChildFolderInChildFolder, "childFileInOtherChildFolderInChildFolder.txt" );
		assertThat( childFileInOtherChildFolderInChildFolder.createNewFile() ).isTrue();
	}

	@Test
	void dotOnlySegmentsInPatternNotAllowed() {
		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> assertPattern( "." ) );
		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> assertPattern( ".." ) );
		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> assertPattern( "*/../test" ) );
		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> assertPattern( "test/." ) );
	}

	@Test
	void walkWithoutPattern() {
		assertPattern( "childFolder" ).visits( 0 ).matches( childFolder );
		assertPattern( "childFile.txt" ).visits( 0 ).matches( childFile );
		assertPattern( "childFile.txt/" ).visits( 0 ).hasNoResults();
		assertPattern( "childFolder/childFileInChildFolder.txt" ).visits( 0 ).matches( childFileInChildFolder );
		assertPattern( "/childFolder/childFolderInChildFolder/" ).visits( 0 ).matches( childFolderInChildFolder );
		assertPattern( "/childFolder/childFolderInChildFolder" ).visits( 0 ).matches( childFolderInChildFolder );
		assertPattern( "childFolder/childFolderInChildFolder" ).visits( 0 ).matches( childFolderInChildFolder );
		assertPattern( "/childFolder/childFolderInChildFolder/childFileInChildFolderInChildFolder.txt" )
				.visits( 0 ).matches( childFileInChildFolderInChildFolder );

		assertPattern( "idontexist" ).visits( 0 ).hasNoResults();
		assertPattern( "/somefolder/idontexist" ).visits( 0 ).hasNoResults();
		assertPattern( "somefolder/idontexist/" ).visits( 0 ).hasNoResults();
	}

	@Test
	void recurseEverything() {
		assertPattern( "**" )
				.visits( 8 )
				.matches( childFile, childFolder, childFileInChildFolder, childFolderInChildFolder, childFileInChildFolderInChildFolder,
				          otherChildFolderInChildFolder, childFileInOtherChildFolderInChildFolder );

		assertPattern( "**/" )
				.visits( 8 )
				.matches( childFolder, childFolderInChildFolder, otherChildFolderInChildFolder );
	}

	@Test
	void singleWildcard() {
		assertPattern( "*" )
				.visits( 3 )
				.matches( childFile, childFolder );

		assertPattern( "*/" )
				.visits( 3 )
				.matches( childFolder );
	}

	@Test
	void partialWildcards() {
		assertPattern( "child*" ).visits( 3 ).matches( childFile, childFolder );
		assertPattern( "child*/" ).visits( 3 ).matches( childFolder );
		assertPattern( "child*/*.txt" ).visits( 6 ).matches( childFileInChildFolder );
		assertPattern( "childFolde?/*.txt" ).visits( 6 ).matches( childFileInChildFolder );
		assertPattern( "childFolde?/*/" ).visits( 6 ).matches( childFolderInChildFolder, otherChildFolderInChildFolder );

		assertPattern( "*.txt" ).visits( 3 ).matches( childFile );
		assertPattern( "*/*.txt" ).visits( 6 ).matches( childFileInChildFolder );
		assertPattern( "*/*/*.txt" ).visits( 8 ).matches( childFileInChildFolderInChildFolder, childFileInOtherChildFolderInChildFolder );
		assertPattern( "*/childFolderInChildFolder/*" ).visits( 7 ).matches( childFileInChildFolderInChildFolder );
		assertPattern( "**/*.txt" )
				.visits( 8 )
				.matches( childFile, childFileInChildFolder, childFileInChildFolderInChildFolder, childFileInOtherChildFolderInChildFolder );
	}

	@Test
	void fixedPrefix() {
		assertPattern( "childFolder/*.txt" ).visits( 4 ).matches( childFileInChildFolder );
		assertPattern( "childFolder/*/" ).visits( 4 ).matches( childFolderInChildFolder, otherChildFolderInChildFolder );
		assertPattern( "childFolder/**" )
				.visits( 6 )
				.matches( childFileInChildFolder, childFolderInChildFolder, childFileInChildFolderInChildFolder, otherChildFolderInChildFolder,
				          childFileInOtherChildFolderInChildFolder );
		assertPattern( "childFolder/childFolderInChildFolder/**" ).visits( 2 ).matches( childFileInChildFolderInChildFolder );
		assertPattern( "childFolder/childFolderInChildFolder/**/" ).visits( 2 ).hasNoResults();
	}

	private PatternTest assertPattern( String childFolder ) {
		return new PatternTest( childFolder );
	}

	@SuppressWarnings("UnusedReturnValue")
	class PatternTest
	{
		private final int visits;
		private final Set<Path> matches = new HashSet<>();

		PatternTest( String pattern ) {
			visits = AntPathMatchingFileVisitor.walkFileTree( tempDir.toPath(), pattern, matches::add );
		}

		PatternTest visits( int numberOfFilesVisited ) {
			assertThat( visits ).isEqualTo( numberOfFilesVisited );
			return this;
		}

		PatternTest matches( File... expectedResults ) {
			assertThat( matches.size() ).isEqualTo( expectedResults.length );
			Stream.of( expectedResults ).map( File::toPath ).forEach( p -> assertThat( matches ).contains( p ) );
			return this;
		}

		PatternTest hasNoResults() {
			assertThat( matches ).isEmpty();
			return this;
		}
	}
}
