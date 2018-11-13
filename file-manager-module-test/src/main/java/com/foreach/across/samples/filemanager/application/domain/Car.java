package com.foreach.across.samples.filemanager.application.domain;

import com.foreach.across.modules.filemanager.business.reference.FileReference;
import com.foreach.across.modules.hibernate.business.SettableIdBasedEntity;
import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Steven Gentens
 * @since 1.3.0
 */
@Entity
@Table(name = "test_car")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Car extends SettableIdBasedEntity<Car>
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_test_car_id")
	@GenericGenerator(
			name = "seq_test_car_id",
			strategy = AcrossSequenceGenerator.STRATEGY,
			parameters = {
					@org.hibernate.annotations.Parameter(name = "sequenceName", value = "seq_test_car_id"),
					@org.hibernate.annotations.Parameter(name = "allocationSize", value = "1")
			}
	)
	private Long id;

	@NotBlank
	@Length(max = 200)
	@Column(name = "name")
	private String name;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "manual_id", referencedColumnName = "id")
	private FileReference manual;

	@NotNull
	@NotEmpty
	@OrderColumn
	@ManyToMany
	@Setter(AccessLevel.NONE)
	@JoinTable(
			name = "car_attachments",
			joinColumns = @JoinColumn(name = "fr_car_car_id"),
			inverseJoinColumns = @JoinColumn(name = "fr_car_fr_id")
	)
	private List<FileReference> attachments;

	public void setAttachments( List<FileReference> attachments ) {
		this.attachments = attachments.stream().filter( Objects::nonNull ).collect( Collectors.toList() );
	}

	@OrderColumn
	@ManyToMany
	@Setter(AccessLevel.NONE)
	@JoinTable(
			name = "car_other_attachments",
			joinColumns = @JoinColumn(name = "fr_car_car_id"),
			inverseJoinColumns = @JoinColumn(name = "fr_car_fr_id")
	)
	private List<FileReference> other;

	public void setOther( List<FileReference> other ) {
		this.other = other.stream().filter( Objects::nonNull ).collect( Collectors.toList() );
	}

	@Lob
	@ElementCollection(targetClass = Document.class)
	@CollectionTable(name = "documents", joinColumns = @JoinColumn(name = "car_id"))
	@Column(name = "documents")
	@Builder.Default
	private List<Document> documents = new ArrayList<>();

	@Embeddable
	@Data
	public static class Document
	{
		@NotNull
		@ManyToOne
		private FileReference file;

		private String description;
	}
}
