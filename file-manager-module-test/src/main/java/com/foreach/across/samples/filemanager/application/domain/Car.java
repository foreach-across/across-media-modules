package com.foreach.across.samples.filemanager.application.domain;

import com.foreach.across.modules.filemanager.business.reference.FileReference;
import com.foreach.across.modules.hibernate.business.SettableIdBasedEntity;
import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
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

	@Column(name = "name")
	private String name;

	@ManyToOne
	@JoinColumn(name = "manual_id", referencedColumnName = "id")
	private FileReference manual;

	@OrderColumn
	@ManyToMany
	@Setter(AccessLevel.NONE)
	@JoinTable(
			name = "test_fileref_car",
			joinColumns = @JoinColumn(name = "fr_car_car_id"),
			inverseJoinColumns = @JoinColumn(name = "fr_car_fr_id")
	)
	private List<FileReference> attachments;

	public void setAttachments( List<FileReference> attachments ) {
		this.attachments = attachments.stream().filter( Objects::nonNull ).collect( Collectors.toList() );
	}
}
