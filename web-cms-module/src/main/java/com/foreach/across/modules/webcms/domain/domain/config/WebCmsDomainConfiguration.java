/*
 * Copyright 2017 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foreach.across.modules.webcms.domain.domain.config;

import com.foreach.across.condition.ConditionalOnConfigurableServletContext;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.support.ReadableAttributes;
import com.foreach.across.core.support.WritableAttributes;
import com.foreach.across.modules.adminweb.config.support.AdminWebConfigurerAdapter;
import com.foreach.across.modules.entity.EntityAttributes;
import com.foreach.across.modules.entity.actions.EntityConfigurationAllowableActionsBuilder;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.entity.query.*;
import com.foreach.across.modules.entity.registry.*;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.registry.properties.MutableEntityPropertyDescriptor;
import com.foreach.across.modules.entity.registry.properties.MutableEntityPropertyRegistry;
import com.foreach.across.modules.entity.views.DispatchingEntityViewFactory;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.EntityViewFactory;
import com.foreach.across.modules.entity.views.processors.DefaultEntityFetchingViewProcessor;
import com.foreach.across.modules.entity.views.processors.EntityQueryFilterProcessor;
import com.foreach.across.modules.entity.views.processors.support.EntityViewProcessorRegistry;
import com.foreach.across.modules.web.mvc.InterceptorRegistry;
import com.foreach.across.modules.webcms.WebCmsEntityAttributes;
import com.foreach.across.modules.webcms.WebCmsEntityAttributes.MultiDomainConfiguration;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.domain.*;
import com.foreach.across.modules.webcms.domain.domain.support.CurrentDomainAwareAllowableActionsBuilder;
import com.foreach.across.modules.webcms.domain.domain.support.CurrentDomainAwareEntityFactory;
import com.foreach.across.modules.webcms.domain.domain.web.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleContextResolver;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.util.Collections;

/**
 * Performs the {@link WebCmsDomain} related configuration.
 * Creates the default {@link WebCmsMultiDomainConfiguration} if none has been specified,
 * and configures the administration UI accordingly if necessary.
 *
 * @author Arne Vandamme
 * @see WebCmsMultiDomainConfiguration
 * @since 0.0.3
 */
@Configuration
class WebCmsDomainConfiguration
{
	@Bean
	@Exposed
	@ConditionalOnMissingBean(WebCmsMultiDomainConfiguration.class)
	public WebCmsMultiDomainConfiguration multiDomainConfiguration() {
		return WebCmsMultiDomainConfiguration.disabled().build();
	}

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	@ConditionalOnConfigurableServletContext
	@ConditionalOnExpression("@multiDomainConfiguration.domainContextFilterClass != null")
	public FilterRegistrationBean siteConfigurationFilter( BeanFactory beanFactory, WebCmsMultiDomainConfiguration multiDomainConfiguration ) {
		Class<? extends Filter> filterClass = multiDomainConfiguration.getDomainContextFilterClass();

		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setName( AbstractWebCmsDomainContextFilter.FILTER_NAME );
		registration.setFilter( beanFactory.getBean( filterClass ) );
		registration.setAsyncSupported( true );
		registration.setMatchAfter( false );
		registration.setUrlPatterns( Collections.singletonList( "/*" ) );
		registration.setDispatcherTypes( DispatcherType.REQUEST, DispatcherType.ERROR, DispatcherType.ASYNC );
		registration.setOrder( Ordered.HIGHEST_PRECEDENCE );

		return registration;
	}

	@Bean(DispatcherServlet.LOCALE_RESOLVER_BEAN_NAME)
	@Primary
	@Exposed
	@ConditionalOnExpression("not @multiDomainConfiguration.disabled")
	@ConditionalOnMissingBean(name = DispatcherServlet.LOCALE_RESOLVER_BEAN_NAME)
	public LocaleContextResolver localeResolver( WebCmsDomainLocaleContextResolver domainLocaleContextResolver ) {
		return domainLocaleContextResolver;
	}

	@Slf4j
	@RequiredArgsConstructor
	@ConditionalOnAdminUI
	@Configuration
	static class AdminUiConfiguration extends AdminWebConfigurerAdapter implements EntityConfigurer
	{
		private static final String PROPERTY = "domain";

		private final EntityRegistry entityRegistry;
		private final AutowireCapableBeanFactory beanFactory;
		private final WebCmsMultiDomainService multiDomainService;
		private final WebCmsMultiDomainConfiguration multiDomainConfiguration;

		@Override
		public void addInterceptors( InterceptorRegistry interceptorRegistry ) {
			if ( !multiDomainConfiguration.isDisabled() ) {
				WebCmsDomainContextHandlerInterceptor interceptor
						= new WebCmsDomainContextHandlerInterceptor( beanFactory.getBean( "adminWebCookieDomainResolver", WebCmsDomainContextResolver.class ) );

				interceptorRegistry.addFirst( interceptor );
			}
		}

		@Bean
		@Lazy
		public CookieWebCmsDomainContextResolver adminWebCookieDomainResolver( WebCmsDomainService domainService ) {
			return new CookieWebCmsDomainContextResolver( domainService );
		}

		@Override
		public void configure( EntitiesConfigurationBuilder entities ) {
			entities.withType( WebCmsDomain.class )
			        .attribute( WebCmsEntityAttributes.DOMAIN_PROPERTY, "id" )
			        .properties( props -> props.property( "attributes" ).hidden( true ).and()
			                                   .property( "id" )
			                                   .attribute( EntityQueryConditionTranslator.class, EntityQueryWebCmsDomainFunctions.conditionTranslator() )
			        );

			entities.assignableTo( WebCmsDomainBound.class )
			        .properties( props -> props.property( "domain" )
			                                   .attribute( EntityQueryConditionTranslator.class, EntityQueryWebCmsDomainFunctions.conditionTranslator() )
			                                   .order( Ordered.HIGHEST_PRECEDENCE ) );

			// always hide domain entity management by default - assume domains will be imported
			entities.withType( WebCmsDomain.class ).hide();

			if ( multiDomainConfiguration.isDisabled() ) {
				hideDomainRelatedConfiguration( entities );
			}
			else if ( multiDomainConfiguration.isDomainSelectablePerEntity() ) {
				configureDomainManagementPerEntity( entities );
			}
			else {
				configureDomainManagementPerDomain( entities );
			}
		}

		private void configureDomainManagementPerEntity( EntitiesConfigurationBuilder entities ) {
			EntityQueryCondition domainCondition = new EntityQueryCondition( "id", EntityQueryOps.IN, new EQValue( "accessibleDomains()" ) );
			EntityQueryCondition boundObjectCondition = new EntityQueryCondition( "domain", EntityQueryOps.IN, new EQValue( "accessibleDomains()" ) );

			entities.all()
			        .postProcessor( cfg -> {
				        if ( shouldAutoConfigure( cfg, MultiDomainConfiguration.FINISHED ) ) {
					        Class<?> entityType = cfg.getEntityType();
					        MutableEntityPropertyRegistry propertyRegistry = cfg.getPropertyRegistry();

					        if ( WebCmsDomain.class.isAssignableFrom( entityType ) ) {
						        adjustOptionsQuery( cfg, translateDomainCondition( cfg, domainCondition ), true );
						        adjustListView( cfg, translateDomainCondition( cfg, domainCondition ) );
					        }
					        else if ( WebCmsDomainBound.class.isAssignableFrom( entityType ) ) {
						        if ( !multiDomainConfiguration.isDomainBound( entityType ) ) {
							        MutableEntityPropertyDescriptor prop = propertyRegistry.getProperty( PROPERTY );
							        prop.setReadable( false );
							        prop.setWritable( false );
							        prop.setHidden( true );
						        }
						        else {
							        adjustOptionsQuery( cfg, translateDomainCondition( cfg, boundObjectCondition ), true );
							        adjustListView( cfg, translateDomainCondition( cfg, boundObjectCondition ) );
						        }
					        }

					        // all properties referring WebCmsDomain should have their options filtered using the domain condition
					        propertyRegistry.getProperties()
					                        .stream()
					                        .filter( p -> p.getPropertyType() != null )
					                        .filter( p -> WebCmsDomain.class.isAssignableFrom( p.getPropertyType() ) )
					                        .map( p -> propertyRegistry.getProperty( p.getName() ) )
					                        .forEach( p -> adjustOptionsQuery( p, translateDomainCondition( p, domainCondition ), false ) );

					        // all properties referring a domain bound object should have their options filtered using the bound object condition
					        propertyRegistry.getProperties()
					                        .stream()
					                        .filter( p -> p.getPropertyType() != null )
					                        .filter(
							                        p -> WebCmsDomainBound.class.isAssignableFrom( p.getPropertyType() )
									                        && multiDomainConfiguration.isDomainBound( p.getPropertyType() )
					                        )
					                        .map( p -> propertyRegistry.getProperty( p.getName() ) )
					                        .forEach( p -> adjustOptionsQuery( p, translateDomainCondition( p, boundObjectCondition ), false ) );

					        // filter associations linking to domain
					        cfg.getAssociations()
					           .stream()
					           .map( MutableEntityAssociation.class::cast )
					           .filter( a -> filterType( a, WebCmsDomain.class, false ) && a.hasView( EntityView.LIST_VIEW_NAME ) )
					           .forEach( a -> adjustListView( a, translateDomainCondition( a, domainCondition ) ) );

					        // filter associations linking to domain bound entity
					        cfg.getAssociations()
					           .stream()
					           .map( MutableEntityAssociation.class::cast )
					           .filter( a -> filterType( a, WebCmsDomainBound.class, true ) && a.hasView( EntityView.LIST_VIEW_NAME ) )
					           .forEach( a -> adjustListView( a, translateDomainCondition( a, boundObjectCondition ) ) );
				        }
			        } );
		}

		private void configureDomainManagementPerDomain( EntitiesConfigurationBuilder entities ) {
			hideDomainRelatedConfiguration( entities );

			EntityQueryCondition domainCondition = new EntityQueryCondition( "id", EntityQueryOps.IN, new EQValue( "visibleDomains()" ) );
			EntityQueryCondition visibleDomainsCondition = new EntityQueryCondition( "domain", EntityQueryOps.IN, new EQValue( "visibleDomains()" ) );
			EntityQueryCondition selectedDomainCondition = new EntityQueryCondition( "domain", EntityQueryOps.EQ, new EQValue( "selectedDomain()" ) );

			entities.all()
			        .postProcessor( cfg -> {
				        if ( shouldAutoConfigure( cfg, MultiDomainConfiguration.FINISHED ) ) {
					        Class<?> entityType = cfg.getEntityType();
					        MutableEntityPropertyRegistry propertyRegistry = cfg.getPropertyRegistry();

					        if ( WebCmsDomain.class.isAssignableFrom( entityType ) ) {
						        adjustOptionsQuery( cfg, translateDomainCondition( cfg, domainCondition ), true );
					        }
					        else if ( multiDomainConfiguration.isDomainBound( entityType ) ) {
						        adjustOptionsQuery( cfg, translateDomainCondition( cfg, visibleDomainsCondition ), true );
						        adjustListView( cfg, translateDomainCondition( cfg, selectedDomainCondition ) );
						        registerDomainAwareEntityFactory( cfg );
						        registerEntityQueryConditionTranslator( cfg );
					        }

					        if ( !WebCmsDomain.class.isAssignableFrom( entityType )
							        && shouldAutoConfigure( cfg, MultiDomainConfiguration.ALLOWABLE_ACTIONS_ADJUSTED ) ) {
						        cfg.setAllowableActionsBuilder( createDomainBasedAllowableActionsBuilder( cfg.getAllowableActionsBuilder() ) );
					        }

					        // all properties referring WebCmsDomain should have their options filtered using the domain condition
					        propertyRegistry.getProperties()
					                        .stream()
					                        .filter( p -> p.getPropertyType() != null )
					                        .filter( p -> WebCmsDomain.class.isAssignableFrom( p.getPropertyType() ) )
					                        .map( p -> propertyRegistry.getProperty( p.getName() ) )
					                        .forEach( p -> adjustOptionsQuery( p, translateDomainCondition( p, domainCondition ), false ) );

					        // all properties referring a domain bound object should have their options filtered using the bound object condition
					        propertyRegistry.getProperties()
					                        .stream()
					                        .filter( p -> p.getPropertyType() != null )
					                        .filter(
							                        p -> WebCmsDomainBound.class.isAssignableFrom( p.getPropertyType() )
									                        && multiDomainConfiguration.isDomainBound( p.getPropertyType() )
					                        )
					                        .map( p -> propertyRegistry.getProperty( p.getName() ) )
					                        .forEach( p -> adjustOptionsQuery( p, translateDomainCondition( p, visibleDomainsCondition ), false ) );

					        // filter associations linking to domain
					        cfg.getAssociations()
					           .stream()
					           .map( MutableEntityAssociation.class::cast )
					           .filter( a -> filterType( a, WebCmsDomain.class, false ) && a.hasView( EntityView.LIST_VIEW_NAME ) )
					           .forEach( a -> adjustListView( a, translateDomainCondition( a, domainCondition ) ) );

					        // filter associations linking to domain bound entity
					        cfg.getAssociations()
					           .stream()
					           .map( MutableEntityAssociation.class::cast )
					           .filter( a -> filterType( a, WebCmsDomainBound.class, true ) && a.hasView( EntityView.LIST_VIEW_NAME ) )
					           .forEach( a -> adjustListView( a, translateDomainCondition( a, selectedDomainCondition ) ) );
				        }
			        } );
		}

		/**
		 * Register the {@link EntityQueryWebCmsDomainFunctions#conditionTranslator()} on any custom domain attribute.
		 */
		private void registerEntityQueryConditionTranslator( MutableEntityConfiguration<Object> entityConfiguration ) {
			if ( entityConfiguration.hasAttribute( WebCmsEntityAttributes.DOMAIN_PROPERTY ) ) {
				String domainProperty = entityConfiguration.getAttribute( WebCmsEntityAttributes.DOMAIN_PROPERTY, String.class );
				MutableEntityPropertyDescriptor propertyDescriptor = entityConfiguration.getPropertyRegistry().getProperty( domainProperty );

				if ( !propertyDescriptor.hasAttribute( EntityQueryConditionTranslator.class ) ) {
					propertyDescriptor.setAttribute( EntityQueryConditionTranslator.class, EntityQueryWebCmsDomainFunctions.conditionTranslator() );
				}
			}
		}

		@SuppressWarnings("unchecked")
		private void registerDomainAwareEntityFactory( MutableEntityConfiguration entityConfiguration ) {
			if ( WebCmsDomainBound.class.isAssignableFrom( entityConfiguration.getEntityType() )
					&& shouldAutoConfigure( entityConfiguration, MultiDomainConfiguration.ENTITY_MODEL_ADJUSTED ) ) {
				PersistentEntity<WebCmsDomainBound, ?> persistentEntity = entityConfiguration.getAttribute( PersistentEntity.class );

				if ( persistentEntity != null ) {
					PersistentEntityFactory<WebCmsDomainBound> pef = new PersistentEntityFactory<>( persistentEntity );
					EntityModel entityModel = entityConfiguration.getEntityModel();

					if ( entityModel instanceof DefaultEntityModel ) {
						( (DefaultEntityModel<WebCmsDomainBound, ?>) entityModel )
								.setEntityFactory( new CurrentDomainAwareEntityFactory( multiDomainService, pef ) );
					}
				}
			}
		}

		private EntityConfigurationAllowableActionsBuilder createDomainBasedAllowableActionsBuilder( EntityConfigurationAllowableActionsBuilder original ) {
			return new CurrentDomainAwareAllowableActionsBuilder( multiDomainService, original );
		}

		private boolean filterType( EntityAssociation association, Class<?> expectedType, boolean requireDomainBound ) {
			return association.getTargetEntityConfiguration() != null
					&& association.getTargetEntityConfiguration().getEntityType() != null
					&& association.getTargetProperty() != null
					&& expectedType.isAssignableFrom( association.getTargetEntityConfiguration().getEntityType() )
					&& ( !requireDomainBound || multiDomainConfiguration.isDomainBound( association.getTargetEntityConfiguration().getEntityType() ) );
		}

		private void adjustOptionsQuery( WritableAttributes owner, EntityQueryCondition condition, boolean always ) {
			if ( condition != null && shouldAutoConfigure( owner, MultiDomainConfiguration.OPTIONS_QUERY_ADJUSTED ) ) {
				Object currentQuery = owner.getAttribute( EntityAttributes.OPTIONS_ENTITY_QUERY );
				Object newQuery = null;
				if ( currentQuery != null ) {
					if ( currentQuery instanceof String ) {
						newQuery = "(" + currentQuery + ") and (" + condition + ")";
					}
					else if ( currentQuery instanceof EntityQuery ) {
						newQuery = EntityQuery.and(
								(EntityQuery) currentQuery,
								new EntityQueryCondition( condition.getProperty(), condition.getOperand(), condition.getArguments() )
						);
					}
				}
				else if ( always ) {
					newQuery = condition.toString();
				}

				if ( newQuery != null ) {
					owner.setAttribute( EntityAttributes.OPTIONS_ENTITY_QUERY, newQuery );
				}
			}
		}

		private <V extends EntityViewRegistry & WritableAttributes> void adjustListView( V registry, EntityQueryCondition condition ) {
			if ( condition != null && shouldAutoConfigure( registry, MultiDomainConfiguration.LIST_VIEW_ADJUSTED ) ) {
				EntityViewFactory viewFactory = registry.getViewFactory( EntityView.LIST_VIEW_NAME );
				if ( viewFactory != null && viewFactory instanceof DispatchingEntityViewFactory ) {
					EntityViewProcessorRegistry processors = ( (DispatchingEntityViewFactory) viewFactory ).getProcessorRegistry();
					processors.getProcessor( EntityQueryFilterProcessor.class.getName(), EntityQueryFilterProcessor.class )
					          .ifPresent( pp -> pp.setBaseEqlPredicate( condition.toString() ) );
					processors.getProcessor( DefaultEntityFetchingViewProcessor.class.getName(), DefaultEntityFetchingViewProcessor.class )
					          .ifPresent( pp -> pp.setBaseEqlPredicate( condition.toString() ) );
				}
			}
		}

		private EntityQueryCondition translateDomainCondition( EntityAssociation entityAssociation, EntityQueryCondition condition ) {
			return translateDomainCondition( entityAssociation.getTargetEntityConfiguration(), condition );
		}

		private EntityQueryCondition translateDomainCondition( EntityPropertyDescriptor propertyDescriptor, EntityQueryCondition condition ) {
			return translateDomainCondition( entityRegistry.getEntityConfiguration( propertyDescriptor.getPropertyType() ), condition );
		}

		private EntityQueryCondition translateDomainCondition( EntityConfiguration entityConfiguration, EntityQueryCondition condition ) {
			return translateDomainCondition( entityConfiguration.getEntityType(), entityConfiguration, condition );
		}

		/**
		 * Translate a template condition to the correct property.
		 * The template condition represents a {@link WebCmsDomainBound} type, so if the type matches that condition will be returned.
		 * Else an explicit {@link WebCmsEntityAttributes#DOMAIN_PROPERTY} is required, if it's missing the returned condition will be null.
		 */
		private EntityQueryCondition translateDomainCondition( Class<?> entityType, ReadableAttributes owner, EntityQueryCondition condition ) {
			String domainProperty = owner.getAttribute( WebCmsEntityAttributes.DOMAIN_PROPERTY, String.class );

			if ( WebCmsDomainBound.class.isAssignableFrom( entityType ) || domainProperty != null ) {
				if ( domainProperty != null ) {
					return new EntityQueryCondition( domainProperty, condition.getOperand(), condition.getArguments() );
				}

				return condition;
			}

			return null;
		}

		/**
		 * Check an auto-configuration adjusted attribute has not yet been set, and sets it at the same time.
		 * If this method returns false, auto-configuration of that segment should be skipped.
		 * <p/>
		 * Use this method as the last condition in a boolean AND expression.
		 */
		private boolean shouldAutoConfigure( WritableAttributes attributes, String attributeName ) {
			if ( !Boolean.TRUE.equals( attributes.getAttribute( attributeName ) ) ) {
				attributes.setAttribute( attributeName, true );
				return true;
			}
			return false;
		}

		private void hideDomainRelatedConfiguration( EntitiesConfigurationBuilder entities ) {
			entities.assignableTo( WebCmsDomainBound.class )
			        .properties( props -> props.property( PROPERTY ).readable( false ).writable( false ).hidden( true ) );
		}
	}
}
