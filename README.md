![Build Status](https://github.com/7mza/authumn/actions/workflows/build.yml/badge.svg) ![Coverage](https://github.com/7mza/authumn/blob/badges/jacoco.svg)

# ![](./authumn.png) authumn

lightweight oauth2 authorization server, based
on [Spring Authorization Server](https://github.com/spring-projects/spring-authorization-server)

still in early stages but the purpose is to provide a generic SSO server, that integrate seamlessly with spring
cloud, can be extended with ease & supports:

- rbac
- persistence
- horizontal scaling
- key management
- dynamic oauth2-client creation

### how to use

#### oauth-resource server

can be configured easily (0 code) using [ch4mpy/spring-addons](https://github.com/ch4mpy/spring-addons/)

```yaml
com:
  c4-soft:
    springaddons:
      oidc:
        ops:
          - iss: http://${authum.host}:${authumn.port}
            authorities:
              # this will allow you to map any claim in access token to GrantedAuthorities
              # that can be used following SPEL @*Authorize("hasAuthority('role')")
              - path: $.scope
                #caze: upper
                #prefix: AUTH_
              - path: $.roles
        resourceserver:
          permit-all:
            - /actuator/health
            - ...
```

#### oauth2-client app

can also be configured using spring-addons or using sboot + yml if you need more ctrl

```kotlin
// this is for webflux, adapt it if using servlet

@Bean
fun tokenSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
    http
        .securityMatcher(PathPatternParserServerWebExchangeMatcher("/oauth2/token", HttpMethod.POST))
        .authorizeExchange { it.anyExchange().permitAll() }
        .csrf { it.disable() } // if you need to use client credentials flow for testing or M2M
    return http.build()
}

@Bean
fun defaultSecurityFilterChain(
    http: ServerHttpSecurity,
    @Value("\${spring.security.permit-all}") permitAll: Array<String>,
    @Value("\${spring.security.permit-origins}") origins: Array<String>,
): SecurityWebFilterChain {
    http
        .authorizeExchange {
            it
                .pathMatchers(*permitAll)
                .permitAll()
                .anyExchange()
                .authenticated()
        }
        .oauth2ResourceServer { it.jwt(Customizer.withDefaults()) } // if your client is also a gateway that need to forward tokens
        .oauth2Client(Customizer.withDefaults())
        .oauth2Login(Customizer.withDefaults()) // if you want your client to use auth server html
        .csrf { it.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse()) }
        .cors { it.configurationSource(urlBasedCorsConfigurationSource(origins)) }
    // .requestCache { it.disable() }
    return http.build()
}

fun urlBasedCorsConfigurationSource(origins: Array<String>): UrlBasedCorsConfigurationSource =
    UrlBasedCorsConfigurationSource().apply {
        registerCorsConfiguration(
            "/**",
            CorsConfiguration().apply {
                allowedOriginPatterns = origins.asList()
                setAllowedMethods(listOf("*"))
                allowedHeaders = listOf("*")
                exposedHeaders = listOf("*")
            },
        )
    }
```

```yaml
# your own clients

spring:
  security:
    oauth2:
      client:
        provider:
          spring:
            issuer-uri: http://${authumn.host}:${authumn.port}
        registration:
          client-credentials:
            authorization-grant-type: client_credentials
            client-id: client-credentials
            client-name: client-credentials
            client-secret: secret
            provider: spring
            scope: role1, role2, ...
          client-authorization:
            authorization-grant-type: authorization_code
            client-authentication-method: client_secret_post
            client-id: client-authorization
            client-name: client-authorization
            client-secret: secret
            provider: spring
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            scope: openid, profile, ...
      resourceserver:
        jwt:
          issuer-uri: http://${authumn.host}:${authumn.port}
    permit-all: /actuator/health, ...
    permit-origins: "*"
```

### how to h scale

if using oauth2Login in your client apps, authorization flow happens via user-agent redirection, auth-server need to be
exposed. you can hide your instances behind a gateway or a lb like [haproxy example](./haproxy/authumn/haproxy.cfg) &
just
scale using
docker

```shell
docker compose up --scale authumn=N --build
```

then have your resource servers and oauth client apps point to the lb

### TODO

- htmx/thymeleaf crud
- account creation validation by email
- pwd validation
- email & pwd change flow
- paging & sorting
- search & filter
- caching
- redis/postgres replication
- move to coroutines
- simple ldap integration
-

### build

```shell
./gradlew clean ktlintFormat ktlintCheck build
```

### run

```shell
docker compose up --build
```

init data is configured in [application.yml](./src/main/resources/application.yml), will populate db if run with
profile "init"

add `-Dspring.profiles.active=default,dev,init` if run with IDE

profile "dev" will empty DB on shutdown

### generate & use a (client credentials) token

```shell
# apt install curl jq
TOKEN=$(curl -k "http://127.0.0.1:9000/oauth2/token" \
-H "Content-Type: application/x-www-form-urlencoded" \
-d "grant_type=client_credentials" \
-d "client_id=client-credentials" \
-d "client_secret=secret2" \
-d "scope=admin user" \
-s | jq .access_token -r)

curl -k "http://127.0.0.1:9000/api/user" \
-H "Accept: application/json" -H "authorization: Bearer $TOKEN"
```

authorization code flow is browser based, it can also be tested
using [ClientAuthorizationTokenTest.kt](src/test/kotlin/com/authumn/authumn/tokens/ClientAuthorizationTokenTest.kt)

### decode token

```shell
# apt install jq
HEADER=$(echo $TOKEN | cut -d '.' -f 1)
PAYLOAD=$(echo $TOKEN | cut -d '.' -f 2)
echo $HEADER | sed 's/-/+/g; s/_/\//g' | base64 -d | jq
echo $PAYLOAD | sed 's/-/+/g; s/_/\//g' | base64 -d | jq
```

### introspect token

```shell
# apt install jq
curl -k "http://127.0.0.1:9000/oauth2/introspect" \
-H "Content-Type: application/x-www-form-urlencoded" \
-H "Accept: application/json" \
-d "token=$TOKEN" \
-d "client_id=client-credentials" \
-d "client_secret=secret2" \
-s | jq
```

### revoke token

```shell
# apt install jq
curl -k "http://127.0.0.1:9000/oauth2/revoke" \
-H "Content-Type: application/x-www-form-urlencoded" \
-d "token=$TOKEN" \
-d "token_type_hint=access_token" \
-d "client_id=client-credentials" \
-d "client_secret=secret2" \
-s
```
