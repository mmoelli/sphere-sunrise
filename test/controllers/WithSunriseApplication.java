package controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.sphere.sdk.categories.Category;
import io.sphere.sdk.categories.CategoryTree;
import io.sphere.sdk.categories.CategoryTreeFactory;
import io.sphere.sdk.client.PlayJavaClient;
import io.sphere.sdk.client.PlayJavaClientImpl;
import io.sphere.sdk.client.SphereRequestExecutor;
import io.sphere.sdk.client.SphereRequestExecutorTestDouble;
import io.sphere.sdk.queries.PagedQueryResult;
import io.sphere.sdk.utils.JsonUtils;
import play.Application;
import play.Configuration;
import plugins.Global;
import play.test.FakeApplication;
import play.test.WithApplication;

import static play.test.Helpers.fakeApplication;

public abstract class WithSunriseApplication extends WithApplication {
    @Override
    protected FakeApplication provideFakeApplication() {
        return fakeApplication(new Global() {
            @Override
            protected Injector createInjector(final Application app) {
                return Guice.createInjector(new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(PlayJavaClient.class).toInstance(injectedClientInstance(app));
                        bind(CategoryTree.class).toInstance(injectedCategoryTree());
                    }
                });
            }
        });
    }

    private CategoryTree injectedCategoryTree() {
        final TypeReference<PagedQueryResult<Category>> typeReference = new TypeReference<PagedQueryResult<Category>>() {

        };
        final PagedQueryResult<Category> categoryPagedQueryResult =
                JsonUtils.readObjectFromJsonFileInClasspath("categories.json", typeReference);
        return CategoryTreeFactory.create(categoryPagedQueryResult.getResults());
    }

    protected PlayJavaClient injectedClientInstance(final Application app){
        return new PlayJavaClientImpl(getConfiguration(app), getSphereRequestExecutor());
    }

    protected SphereRequestExecutor getSphereRequestExecutor() {
        return new SphereRequestExecutorTestDouble() {
        };
    }

    /**
     * Override this to add additional settings
     * @param app the application used
     * @return a configuration containing the {@code app} configuration values and overridden values
     */
    protected Configuration getConfiguration(Application app) {
        return app.configuration();
    }
}
