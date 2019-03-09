@file:Suppress("unused", "FunctionName", "PropertyName")
//package keycloak

import kotlin.js.Promise

class KeycloakInitOptions(
    /**
     * @private Undocumented.
     */
    //private var useNonce: Boolean?

    /**
     * Allows to use different adapter:
     *
     * - {string} default - using browser api for redirects
     * - {string} cordova - using cordova plugins
     * - {function} - allows to provide custom function as adapter.
     */
    var adapter: String = "default",

    /**
     * Specifies an action to do on load. 'login-required'|'check-sso'
     */
    var onLoad: String? = null,

    /**
     * Set an initial value for the token.
     */
    var token: String? = null,

    /**
     * Set an initial value for the refresh token.
     */
    var refreshToken: String? = null,

    /**
     * Set an initial value for the id token (only together with `token` or
     * `refreshToken`).
     */
    var idToken: String? = null,

    /**
     * Set an initial value for skew between local time and Keycloak server in
     * seconds (only together with `token` or `refreshToken`).
     */
    var timeSkew: Number? = null,

    /**
     * Set to enable/disable monitoring login state.
     * @default true
     */
    var checkLoginIframe: Boolean = true,

    /**
     * Set the interval to check login state (in seconds).
     * @default 5
     */
    var checkLoginIframeInterval: Number = 5,

    /**
     * Set the OpenID Connect response mode to send to Keycloak upon login.
     * 'query'|'fragment'
     * @default fragment After successful authentication Keycloak will redirect
     *                   to JavaScript application with OpenID Connect parameters
     *                   added in URL fragment. This is generally safer and
     *                   recommended over query.
     */
    var responseMode: String = "fragment",

    /**
     * Set the OpenID Connect flow ('standard'|'implicit'|'hybrid')
     * @default standard
     */
    var flow: String = "standard"
)

interface KeycloakLoginOptions {
    /**
     * @private Undocumented.
     */
    var scope: String?

    /**
     * Specifies the uri to redirect to after login.
     */
    var redirectUri: String?

    /**
     * By default the login screen is displayed if the user is not logged into
     * Keycloak. To only authenticate to the application if the user is already
     * logged in and not display the login page if the user is not logged in, set
     * this option to `'none'`. To always require re-authentication and ignore
     * SSO, set this option to `'login'`.
     */
    var prompt: String?/*'none'|'login'*/

    /**
     * If value is `'register'` then user is redirected to registration page,
     * otherwise to login page.
     */
    var action: String? /*'register'*/

    /**
     * Used just if user is already authenticated. Specifies maximum time since
     * the authentication of user happened. If user is already authenticated for
     * longer time than `'maxAge'`, the SSO is ignored and he will need to
     * authenticate again.
     */
    var maxAge: Number?

    /**
     * Used to pre-fill the username/email field on the login form.
     */
    var loginHint: String?

    /**
     * Used to tell Keycloak which IDP the user wants to authenticate with.
     */
    var idpHint: String?

    /**
     * Sets the 'ui_locales' query param in compliance with section 3.1.2.1
     * of the OIDC 1.0 specification.
     */
    var locale: String?

    /**
     * Specifies the desired Keycloak locale for the UI.  This differs from
     * the locale param in that it tells the Keycloak server to set a cookie and update
     * the user's profile to a new preferred locale.
     */
    var kcLocale: String?
}

typealias KeycloakPromiseCallback<T> = (result: T) -> Unit

external class KeycloakPromise<TSuccess, TError>: Promise<TSuccess> {
    /**
     * Function to call if the promised action succeeds.
     */
    fun success(callback: KeycloakPromiseCallback<TSuccess>): KeycloakPromise<TSuccess, TError>

    /**
     * Function to call if the promised action throws an error.
     */
    fun error(callback: KeycloakPromiseCallback<TError>): KeycloakPromise<TSuccess, TError>
}

interface KeycloakError {
    var error: String
    var description: String
}

class RedirectUriOptions(val redirectUri: String)

interface KeycloakAdapter {
    var login: ((options: KeycloakLoginOptions?) -> KeycloakPromise<Any, Any>)?
    var logout: ((options: Any?) -> KeycloakPromise<Any, Any>)?
    var register: ((options: KeycloakLoginOptions?) -> KeycloakPromise<Any, Any>)?
    var accountManagement: (() -> KeycloakPromise<Any, Any>)?
    var redirectUri: ((options: RedirectUriOptions, encodeHash: Boolean) -> String)?
}

interface KeycloakProfile {
    var id: String?
    var username: String?
    var email: String?
    var firstName: String?
    var lastName: String?
    var enabled: Boolean?
    var emailVerified: Boolean?
    var totp: Boolean?
    var createdTimestamp: Number?
}

// export interface KeycloakUserInfo {}

class RealmAccess(val roles: Array<String>)

class ParsedToken(
    val exp: Number?,
    val iat: Number?,
    val nonce: String?,
    val sub: String?,
    val session_state: String?,
    val realm_access: RealmAccess?,
    val resource_access: Array<String>?
)

class RefreshParsedToken(val nonce: String)

/**
 * A client for the Keycloak authentication server.
 * @see {@link https://keycloak.gitbooks.io/securing-client-applications-guide/content/topics/oidc/javascript-adapter.html|Keycloak JS adapter documentation}
 */
external interface KeycloakInstance {
    /**
     * Is true if the user is authenticated, false otherwise.
     */
    var authenticated: Boolean

    /**
     * The user id.
     */
    var subject: String?

    /**
     * Response mode passed in init (default value is `'fragment'`).
     */
    var responseMode: String

    /**
     * Response type sent to Keycloak with login requests. This is determined
     * based on the flow value used during initialization, but can be overridden
     * by setting this value.
     *
     * 'code'|'id_token token'|'code id_token token'.
     */
    var responseType: String?

    /**
     * Flow passed in init.
     */
    var flow: String

    /**
     * The realm roles associated with the token.
     */
    var realmAccess: RealmAccess?

    /**
     * The resource roles associated with the token.
     */
    var resourceAccess: Array<String>?

    /**
     * The base64 encoded token that can be sent in the Authorization header in
     * requests to services.
     */
    var token: String?

    /**
     * The parsed token as a JavaScript object.
     */
    var tokenParsed: ParsedToken?

    /**
     * The base64 encoded refresh token that can be used to retrieve a new token.
     */
    var refreshToken: String?

    /**
     * The parsed refresh token as a JavaScript object.
     */
    var refreshTokenParsed: RefreshParsedToken?

    /**
     * The base64 encoded ID token.
     */
    var idToken: String?

    /**
     * The parsed id token as a JavaScript object.
     */
    var idTokenParsed: RefreshParsedToken?

    /**
     * The estimated time difference between the browser time and the Keycloak
     * server in seconds. This value is just an estimation, but is accurate
     * enough when determining if a token is expired or not.
     */
    var timeSkew: Number?

    /**
     * @private Undocumented.
     */
    var loginRequired: Boolean?

    /**
     * @private Undocumented.
     */
    var authServerUrl: String?

    /**
     * @private Undocumented.
     */
    var realm: String?

    /**
     * @private Undocumented.
     */
    var clientId: String?

    /**
     * @private Undocumented.
     */
    var clientSecret: String?

    /**
     * @private Undocumented.
     */
    var redirectUri: String?

    /**
     * @private Undocumented.
     */
    var sessionId: String?

    /**
     * @private Undocumented.
     */
    var profile: KeycloakProfile?

    /**
     * @private Undocumented.
     */
    var userInfo: Any? // KeycloakUserInfo;
    /**
     * Called when the adapter is initialized.
     */
    var onReady: ((authenticated: Boolean?) -> Unit)?

    /**
     * Called when a user is successfully authenticated.
     */
    var onAuthSuccess: (() -> Unit)?

    /**
     * Called if there was an error during authentication.
     */
    var onAuthError: ((errorData: KeycloakError) -> Unit)?

    /**
     * Called when the token is refreshed.
     */
    var onAuthRefreshSuccess:(() -> Unit)?

    /**
     * Called if there was an error while trying to refresh the token.
     */
    var onAuthRefreshError: (() -> Unit)?

    /**
     * Called if the user is logged out (will only be called if the session
     * status iframe is enabled, or in Cordova mode).
     */
    var onAuthLogout: (() -> Unit)?

    /**
     * Called when the access token is expired. If a refresh token is available
     * the token can be refreshed with Keycloak#updateToken, or in cases where
     * it's not (ie. with implicit flow) you can redirect to login screen to
     * obtain a new access token.
     */
    var onTokenExpired: (() -> Unit)?

    /**
     * Called to initialize the adapter.
     * @param initOptions Initialization options.
     * @returns A promise to set functions to be invoked on success or error.
     */
    fun init(initOptions: KeycloakInitOptions): KeycloakPromise<Boolean, KeycloakError>

    /**
     * Redirects to login form.
     * @param options Login options.
     */
    fun login(options: KeycloakLoginOptions?): KeycloakPromise<Nothing, Nothing>

    /**
     * Redirects to logout.
     * @param options Logout options. `options?.redirectUri` Specifies the uri to redirect to after logout.
     */
    fun logout(options: RedirectUriOptions?): KeycloakPromise<Nothing, Nothing>

    /**
     * Redirects to registration form.
     * @param options Supports same options as Keycloak#login but `action` is
     *                set to `'register'`.
     */
    fun register(options: Any?): KeycloakPromise<Nothing, Nothing>

    /**
     * Redirects to the Account Management Console.
     */
    fun accountManagement(): KeycloakPromise<Nothing, Nothing>

    /**
     * Returns the URL to login form.
     * @param options Supports same options as Keycloak#login.
     */
    fun createLoginUrl(options: KeycloakLoginOptions?): String

    /**
     * Returns the URL to logout the user.
     * @param options Logout options. `options?.redirectUri` Specifies the uri to redirect to after logout.
     */
    fun createLogoutUrl(options: Any?): String

    /**
     * Returns the URL to registration page.
     * @param options Supports same options as Keycloak#createLoginUrl but
     *                `action` is set to `'register'`.
     */
    fun createRegisterUrl(options: KeycloakLoginOptions?): String

    /**
     * Returns the URL to the Account Management Console.
     */
    fun createAccountUrl(): String

    /**
     * Returns true if the token has less than `minValidity` seconds left before
     * it expires.
     * @param minValidity If not specified, `0` is used.
     */
    fun isTokenExpired(minValidity: Number?): Boolean

    /**
     * If the token expires within `minValidity` seconds, the token is refreshed.
     * If the session status iframe is enabled, the session status is also
     * checked.
     * @returns A promise to set functions that can be invoked if the token is
     *          still valid, or if the token is no longer valid.
     * @example
     * ```js
     * keycloak.updateToken(5).success(function(refreshed) {
     *   if (refreshed) {
     *     alert('Token was successfully refreshed');
     *   } else {
     *     alert('Token is still valid');
     *   }
     * }).error(function() {
     *   alert('Failed to refresh the token, or the session has expired');
     * });
     */
    fun updateToken(minValidity: Number): KeycloakPromise<Boolean, Boolean>

    /**
     * Clears authentication state, including tokens. This can be useful if
     * the application has detected the session was expired, for example if
     * updating token fails. Invoking this results in Keycloak#onAuthLogout
     * callback listener being invoked.
     */
    fun clearToken()

    /**
     * Returns true if the token has the given realm role.
     * @param role A realm role name.
     */
    fun hasRealmRole(role: String): Boolean

    /**
     * Returns true if the token has the given role for the resource.
     * @param role A role name.
     * @param resource If not specified, `clientId` is used.
     */
    fun hasResourceRole(role: String, resource: String?): Boolean

    /**
     * Loads the user's profile.
     * @returns A promise to set functions to be invoked on success or error.
     */
    fun loadUserProfile(): KeycloakPromise<KeycloakProfile, Nothing>

    /**
     * @private Undocumented.
     */
    fun loadUserInfo(): KeycloakPromise<Any, Nothing>
}

external fun Keycloak(config: String? = definedExternally): KeycloakInstance
