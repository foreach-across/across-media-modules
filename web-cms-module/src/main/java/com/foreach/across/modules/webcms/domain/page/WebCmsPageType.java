package com.foreach.across.modules.webcms.domain.page;

import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifier;
import lombok.*;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;
import java.util.Map;

/**
 * Type specifier for a particular page.
 * @author Raf Ceuls
 * @since 0.0.2
 */
@NotThreadSafe
@Entity
@DiscriminatorValue(WebCmsPageType.OBJECT_TYPE)
@Table(name = "wcm_page_type")
@Getter
@Setter
@NoArgsConstructor
public class WebCmsPageType extends WebCmsTypeSpecifier<WebCmsPageType>
{
	/**
	 * Object type name (discriminator value).
	 */
	public static final String OBJECT_TYPE = "page";

	/**
	 * Prefix that all object ids of a WebCmsArticleType have.
	 */
	public static final String COLLECTION_ID = "wcm:type:page";

	@Builder(toBuilder = true)
	protected WebCmsPageType( @Builder.ObtainVia(method = "getId") Long id,
	                          @Builder.ObtainVia(method = "getNewEntityId") Long newEntityId,
	                          @Builder.ObtainVia(method = "getObjectId") String objectId,
	                          @Builder.ObtainVia(method = "getCreatedBy") String createdBy,
	                          @Builder.ObtainVia(method = "getCreatedDate") Date createdDate,
	                          @Builder.ObtainVia(method = "getLastModifiedBy") String lastModifiedBy,
	                          @Builder.ObtainVia(method = "getLastModifiedDate") Date lastModifiedDate,
	                          @Builder.ObtainVia(method = "getName") String name,
	                          @Builder.ObtainVia(method = "getTypeKey") String typeKey,
	                          @Singular @Builder.ObtainVia(method = "getAttributes") Map<String, String> attributes ) {
		super( id, newEntityId, objectId, createdBy, createdDate, lastModifiedBy, lastModifiedDate, name, typeKey, attributes );
	}

	@Override
	public final String getObjectType() {
		return OBJECT_TYPE;
	}

	@Override
	protected final String getObjectCollectionId() {
		return COLLECTION_ID;
	}
}

