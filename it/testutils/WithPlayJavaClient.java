package testutils;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.sphere.sdk.client.PlayJavaClient;
import io.sphere.sdk.client.PlayJavaClientImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Iterables.isEmpty;

public abstract class WithPlayJavaClient {
    protected static PlayJavaClient client;

    @BeforeClass
    public static void setupJavaClient() {
        final List<EnvSetting> requiredEnv = requiredEnvironmentVariables();
        requirePresent(requiredEnv);
        final String configAsString = getConfig(requiredEnv);
        final Config config = ConfigFactory.parseString(configAsString).withFallback(ConfigFactory.load());
        client = new PlayJavaClientImpl(config);
    }

    @AfterClass
    public static void stopJavaClient() {
        if (client != null) {
            client.close();
        }

    }

    private static List<EnvSetting> requiredEnvironmentVariables() {
        final String descEnd = " for the integration test SPHERE.IO project.";
        final String prefix = "SPHERE_SUNRISE_IT_TESTS_";
        return Lists.newArrayList(
                new EnvSetting(prefix + "PROJECT", "The project key" + descEnd, "sphere.project"),
                new EnvSetting(prefix + "CLIENT_SECRET", "The client secret" + descEnd, "sphere.clientSecret"),
                new EnvSetting(prefix + "CLIENT_ID", "The client id" + descEnd, "sphere.clientId")
        );
    }

    private static String getConfig(List<EnvSetting> requiredEnv) {
        final Iterable<String> configs = Iterables.transform(requiredEnv, new Function<EnvSetting, String>() {
            @Override
            public String apply(final EnvSetting input) {
                return input.getConfigKey() + "=" + System.getenv(input.getEnvKey());
            }
        });
        return Joiner.on("\n").join(configs);
    }

    private static void requirePresent(List<EnvSetting> requiredEnv) {
        final Iterable<EnvSetting> missingSettings = Iterables.filter(requiredEnv, new Predicate<EnvSetting>() {
            @Override
            public boolean apply(final EnvSetting input) {
                return isNullOrEmpty(System.getenv(input.getEnvKey()));
            }
        });
        final boolean allRequiredEnvironmentVariablesPresent = isEmpty(missingSettings);
        if (!allRequiredEnvironmentVariablesPresent) {
            throw new RuntimeException("Missing environment variables to execute tests:\n" + Joiner.on("\n").join(missingSettings));
        }
    }

    private static class EnvSetting {
        final String key;
        final String descriptions;
        private final String configKey;

        private EnvSetting(String key, String descriptions, String configKey) {
            this.key = key;
            this.descriptions = descriptions;
            this.configKey = configKey;
        }

        public String getEnvKey() {
            return key;
        }

        public String getDescriptions() {
            return descriptions;
        }

        public String getConfigKey() {
            return configKey;
        }

        @Override
        public String toString() {
            return "EnvSetting{" +
                    "key='" + key + '\'' +
                    ", descriptions='" + descriptions + '\'' +
                    '}';
        }
    }
}