package com.foreach.imageserver.core.business;

import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import com.foreach.imageserver.core.config.ImageSchemaConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = ImageSchemaConfiguration.TABLE_CONTEXT)
@Getter
@Setter
public class ImageContext
{
	@Id
	@GeneratedValue(generator = "seq_img_context_id")
	@GenericGenerator(
			name = "seq_img_context_id",
			strategy = AcrossSequenceGenerator.STRATEGY,
			parameters = {
					@org.hibernate.annotations.Parameter(name = "sequenceName", value = "seq_img_context_id"),
					@org.hibernate.annotations.Parameter(name = "allocationSize", value = "10")
			}
	)
	private long id;

	/**
	 * Careful: We use the code to generate an intelligible folder structure. Make sure that it can be used as a valid
	 * folder name.
	 */
	@Column(name = "code")
	private String code;

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( !( o instanceof ImageContext ) ) {
			return false;
		}

		ImageContext that = (ImageContext) o;

		return Objects.equals( this.id, that.id );
	}

	@Override
	public int hashCode() {
		return Objects.hash( id );
	}
}
