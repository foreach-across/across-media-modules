package com.foreach.across.samples.filemanager.application.domain;

import com.foreach.across.modules.hibernate.business.SettableIdBasedEntity;
import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * @author Steven Gentens
 * @since 1.3.0
 */
@Entity
@Table(name = "test_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class User extends SettableIdBasedEntity
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_test_user_id")
	@GenericGenerator(
			name = "seq_test_user_id",
			strategy = AcrossSequenceGenerator.STRATEGY,
			parameters = {
					@org.hibernate.annotations.Parameter(name = "sequenceName", value = "seq_test_user_id"),
					@org.hibernate.annotations.Parameter(name = "allocationSize", value = "1")
			}
	)
	private Long id;

	@Column(name = "name")
	private String name;

	@Embedded
	private FileReferenceId avatarId;
}
