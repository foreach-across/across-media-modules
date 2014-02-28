package com.foreach.imageserver.connectors.dpp;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.services.exceptions.ImageModificationException;
import com.foreach.imageserver.core.services.transformers.ImageModifyAction;
import com.foreach.imageserver.core.services.transformers.ImageTransformer;
import com.foreach.imageserver.core.services.transformers.ImageTransformerAction;
import com.foreach.imageserver.core.services.transformers.ImageTransformerPriority;
import org.apache.pdfbox.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class AssetConversionImageTransformer implements ImageTransformer
{
	public static final int GS_MAX_DENSITY = 1200;
	public static final int GS_DEFAULT_DENSITY = 72;
	public static final int GS_DENSITY_STEP = 300;
	public static final String ALPHA_BACKGROUND = "white";

	private static final Logger LOG = LoggerFactory.getLogger( AssetConversionImageTransformer.class );

	private final int priority;
	private boolean enabled;
	private final String serverUrl;

	@Autowired
	private AssetConversionService assetConversionService;

	public AssetConversionImageTransformer( int priority, boolean enabled, String serverUrl ) {
		this.priority = priority;
		this.enabled = enabled;
		this.serverUrl = serverUrl;
	}

	@Override
	public String getName() {
		return "assetconversion";
	}

	@Override
	public ImageTransformerPriority canExecute( ImageTransformerAction action ) {
		// Only use the AssetConversionService for modifications
		return action instanceof ImageModifyAction ? ImageTransformerPriority.PREFERRED : ImageTransformerPriority.UNABLE;
	}

	public class Parameters
	{
		public void add( String name, String value ) {

		}

		public void add( String name, int value ) {

		}
	}

	@Override
	public void execute( ImageTransformerAction transformerAction ) {
		ImageModifyAction action = (ImageModifyAction) transformerAction;

		try {
			ImageModifier modifier = action.getModifier();

			byte[] sourceData = IOUtils.toByteArray( action.getImageFile().openContentStream() );

			if ( modifier.hasCrop() ) {
				sourceData = executeCrop( sourceData, action.getImageFile(), modifier );
			}

			Parameters parameters = new Parameters();
			Dimensions appliedDensity = setDensityIfRequired( parameters, action.getImageFile(), modifier );

			if ( modifier.hasCrop() ) {
				Crop crop = applyDensity( modifier.getCrop(), appliedDensity );
				parameters.add( "width", crop.getWidth() );
				parameters.add( "height", crop.getHeight() );
				parameters.add( "x", crop.getX() );
				parameters.add( "y", crop.getY() );
			}

			parameters.add( "format", modifier.getOutput().getExtension() );
			parameters.add( "noprofiles", "true" );
			parameters.add( "colorspace", "sRGB" );

			/*ByteArrayPartSource file = new ByteArrayPartSource( "random", data );*/

			/*
					ConvertCmd cmd = new ConvertCmd();

					IMOperation op = new IMOperation();
					Dimensions appliedDensity = setDensityIfRequired( op, action.getImageFile(), modifier );
					op.addImage( "-" );

					if ( shouldRemoveTransparency( action.getImageFile().getImageType(), modifier.getOutput() ) ) {
						op.background( ALPHA_BACKGROUND );
						op.flatten();
					}

					if ( modifier.hasCrop() ) {
						Crop crop = applyDensity( modifier.getCrop(), appliedDensity );
						op.crop( crop.getWidth(), crop.getHeight(), crop.getX(), crop.getY() );
					}

					op.resize( modifier.getWidth(), modifier.getHeight(), "!" );
					op.colorspace( "sRGB" );
					op.addImage( modifier.getOutput().getExtension() + ":-" );

					ByteArrayOutputStream os = new ByteArrayOutputStream();

					cmd.setInputProvider( new Pipe( action.getImageFile().openContentStream(), null ) );
					cmd.setOutputConsumer( new Pipe( null, os ) );

					cmd.run( op );

					byte[] bytes = os.toByteArray();
					ImageFile result =
							new ImageFile( action.getModifier().getOutput(), bytes.length, new ByteArrayInputStream( bytes ) );
					action.setResult( result );*/

			/*byte[] converted = assetConversionService.convert( file, Asset.Format.fromMimeType(
								modifier.getOutput().getContentType() ), conversion );

						ImageFile result = new ImageFile( action.getModifier().getOutput(), converted.length,
						                                  new ByteArrayInputStream( converted ) );
						action.setResult( result );*/
		}
		catch ( Exception e ) {
			LOG.error( "Failed to apply modification {}: {}", action, e );
			throw new ImageModificationException( e );
		}

	}

	private byte[] executeCrop( byte[] source, ImageFile imageFile, ImageModifier modifier ) {
		Parameters parameters = new Parameters();
		Dimensions appliedDensity = setDensityIfRequired( parameters, imageFile, modifier );

		Crop crop = applyDensity( modifier.getCrop(), appliedDensity );
		parameters.add( "width", crop.getWidth() );
		parameters.add( "height", crop.getHeight() );
		parameters.add( "x", crop.getX() );
		parameters.add( "y", crop.getY() );

		parameters.add( "format", modifier.getOutput().getExtension() );
		parameters.add( "noprofiles", "true" );
		parameters.add( "colorspace", "sRGB" );

		return null;

	}

	private Crop applyDensity( Crop crop, Dimensions density ) {
		if ( density != null ) {
			double widthFactor = (double) density.getWidth() / GS_DEFAULT_DENSITY;
			double heightFactor = (double) density.getHeight() / GS_DEFAULT_DENSITY;

			return new Crop( Double.valueOf( widthFactor * crop.getX() ).intValue(),
			                 Double.valueOf( heightFactor * crop.getY() ).intValue(),
			                 Double.valueOf( widthFactor * crop.getWidth() ).intValue(),
			                 Double.valueOf( heightFactor * crop.getHeight() ).intValue() );
		}

		return crop;
	}

	private Dimensions setDensityIfRequired( Parameters parameters, ImageFile original, ImageModifier modifier ) {
		if ( original.getImageType().isScalable() ) {
			Dimensions density = modifier.getDensity();

			if ( density != null && !Dimensions.EMPTY.equals(
					density ) && ( density.getHeight() > 1 || density.getWidth() > 1 ) ) {
				int horizontalDensity = calculateDensity( density.getWidth() );
				int verticalDensity = calculateDensity( density.getHeight() );

				LOG.debug( "Applying density {}x{}", horizontalDensity, verticalDensity );
				parameters.add( "density", horizontalDensity + "x" + verticalDensity );

				return new Dimensions( horizontalDensity, verticalDensity );
			}
		}

		return null;
	}

	private int calculateDensity( int multiplier ) {
		int raw = Math.min( GS_MAX_DENSITY, GS_DEFAULT_DENSITY * Math.max( multiplier, 1 ) );
		int times = raw / GS_DENSITY_STEP;
		int remainder = raw % GS_DENSITY_STEP;

		return remainder == 0 ? raw : Math.min( GS_MAX_DENSITY, ( times + 1 ) * GS_DENSITY_STEP );
	}

	private boolean shouldRemoveTransparency( ImageType original, ImageType requested ) {
		return original.hasTransparency() && !requested.hasTransparency();
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled( boolean enabled ) {
		this.enabled = enabled;
	}
}
