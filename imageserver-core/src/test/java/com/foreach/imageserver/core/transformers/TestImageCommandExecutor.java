package com.foreach.imageserver.core.transformers;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
public class TestImageCommandExecutor
{
	@Test
	public void customCommandExecutorCanOnlyHandleCustomCommands() {
		CustomCommandExecutor customCommandExecutor = new CustomCommandExecutor();
		assertThat( customCommandExecutor.handles( CustomCommand.class ) ).isTrue();
		assertThat( customCommandExecutor.handles( OtherCommand.class ) ).isFalse();
		assertThat( customCommandExecutor.handles( ImageCommand.class ) ).isFalse();
	}

	@Test
	public void otherCommandExecutorCanOnlyHandleCustomCommands() {
		OtherCommandExecutor otherCommandExecutor = new OtherCommandExecutor();
		assertThat( otherCommandExecutor.handles( CustomCommand.class ) ).isFalse();
		assertThat( otherCommandExecutor.handles( OtherCommand.class ) ).isTrue();
		assertThat( otherCommandExecutor.handles( ImageCommand.class ) ).isFalse();
	}

	@Test
	public void allCommandExecutor() {
		AllCommandExecutor allCommandExecutor = new AllCommandExecutor();
		assertThat( allCommandExecutor.handles( CustomCommand.class ) ).isTrue();
		assertThat( allCommandExecutor.handles( OtherCommand.class ) ).isTrue();
		assertThat( allCommandExecutor.handles( ImageCommand.class ) ).isTrue();
	}

	private static class CustomCommand extends ImageCommand
	{
	}

	private static class OtherCommand extends ImageCommand
	{

	}

	private static class CustomCommandExecutor implements ImageCommandExecutor<CustomCommand>
	{
		@Override
		public ImageTransformerPriority canExecute( CustomCommand command ) {
			return null;
		}

		@Override
		public void execute( CustomCommand command ) {

		}
	}

	private static class AllCommandExecutor implements ImageCommandExecutor
	{
		@Override
		public ImageTransformerPriority canExecute( ImageCommand command ) {
			return null;
		}

		@Override
		public void execute( ImageCommand command ) {

		}
	}

	private static class OtherCommandExecutor implements ImageCommandExecutor<OtherCommand>
	{
		@Override
		public ImageTransformerPriority canExecute( OtherCommand command ) {
			return null;
		}

		@Override
		public void execute( OtherCommand command ) {

		}
	}
}
