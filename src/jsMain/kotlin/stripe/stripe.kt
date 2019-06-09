@file:Suppress("unused")

package stripe

import stripe.elements.Element
import stripe.elements.Elements
import kotlin.js.Promise


interface Error {
    val type: String
    val charge: String
    val message: String
    val code: String?
    val decline_code: String?
    val param: String?
}

class TokenOptions(
    val name: String? = null,
    val address_line1: String? = null,
    val address_line2: String? = null,
    val address_city: String? = null,
    val address_state: String? = null,
    val address_zip: String? = null,
    val address_country: String? = null,
    val currency: String? = null
)


external interface BankAccount {
    val id: String
    // TODO object: string;
    val account_holder_name: String
    val account_holder_type: String
    val bank_name: String
    val country: String
    val currency: String
    val fingerprint: String
    val last4: String
    val routing_number: String
    /** Can be 'new' | 'validated' | 'verified' | 'verification_failed' | 'errored' */
    val status: String
}

interface Card {
    val id: String
    // TODO object: string;
    val address_city: String?
    val address_country: String?
    val address_line1: String?
    /** Can be 'pass' | 'fail' | 'unavailable' | 'unchecked' */
    val address_line1_check: String?
    val address_line2: String?
    val address_state: String?
    val address_zip: String?
    /** Can be 'pass' | 'fail' | 'unavailable' | 'unchecked' */
    val address_zip_check: String?
    /** Can be 'Visa' | 'American Express' | 'MasterCard' | 'Discover' | 'JCB' | 'Diners Club' | 'Unknown' */
    val brand: String
    val country: String
    val currency: String?
    /** Can be 'pass' | 'fail' | 'unavailable' | 'unchecked' */
    val cvc_check: String?
    val dynamic_last4: String
    val exp_month: Number
    val exp_year: Number
    val fingerprint: String
    /** Can be 'credit' | 'debit' | 'prepaid' | 'unknown' */
    val funding: String
    val last4: String
    val metadata: Any
    val name: String?
    /** Can be 'apple_pay' | 'android_pay' */
    val tokenization_method: String?
    /** Can be 'required' | 'recommended' | 'optional' | 'not_supported' */
    val three_d_secure: String?
}

interface Token {
    val id: String;
    // TODO object: string;
    val bank_account: BankAccount?
    val card: Card?
    val client_ip: String
    val created: Number
    val livemode: Boolean
    val type: String
    val used: Boolean
}

interface TokenResponse {
    val token: Token?
    val error: Error?
}

external class Stripe(key: String) {

    // TODO elements(options?: elements.ElementsCreateOptions): elements.Elements;
    fun elements(): Elements

    fun createToken(element: Element, options: TokenOptions? = definedExternally): Promise<TokenResponse>
    // TODO createToken(name: 'bank_account', options: BankAccountTokenOptions): Promise<TokenResponse>;
    // TODO createToken(name: 'pii', options: PiiTokenOptions): Promise<TokenResponse>;

}


/*
interface Stripe {
    createSource(element: elements.Element, options?: {owner?: OwnerInfo}): Promise<SourceResponse>;
    createSource(options: SourceOptions): Promise<SourceResponse>;
    retrieveSource(options: RetrieveSourceOptions): Promise<SourceResponse>;
    paymentRequest(options: paymentRequest.StripePaymentRequestOptions): paymentRequest.StripePaymentRequest;
}

interface StripeOptions {
  stripeAccount?: string;
  betas?: string[];
}

interface BankAccountTokenOptions {
    country: string;
    currency: string;
    routing_number: string;
    account_number: string;
    account_holder_name: string;
    account_holder_type: string;
}

interface PiiTokenOptions {
    personal_id_number: string;
}

interface OwnerAddress {
    city?: string;
    country?: string;
    line1?: string;
    line2?: string;
    postal_code?: string;
    state?: string;
}

interface OwnerInfo {
    address?: OwnerAddress;
    name?: string;
    email?: string;
    phone?: string;
}

interface SourceOptions {
    type: string;
    flow?: 'redirect' | 'receiver' | 'code_verification' | 'none';
    sepa_debit?: {
        iban: string;
    };
    currency?: string;
    amount?: number;
    owner?: OwnerInfo;
    metadata?: {};
    statement_descriptor?: string;
    redirect?: {
        return_url: string;
    };
    token?: string;
    usage?: 'reusable' | 'single_use';
    three_d_secure?: {
        card: string;
    };
}


interface Source {
    client_secret: string;
    created: number;
    currency: string;
    id: string;
    owner: {
        address: OwnerAddress | null;
        email: string | null;
        name: string | null;
        phone: string | null;
        verified_address: string | null;
        verified_email: string | null;
        verified_name: string | null;
        verified_phone: string | null;
    };
    sepa_debit?: {
        bank_code: string | null;
        country: string | null;
        fingerprint: string;
        last4: string;
        mandate_reference: string;
    };
    card?: Card;
    status?: string;
    redirect?: {
        status: string;
        url: string;
    };
    three_d_secure?: {
        authenticated: boolean;
    };
}

interface SourceResponse {
    source?: Source;
    error?: Error;
}

interface RetrieveSourceOptions {
    id: string;
    client_secret: string;
}

// Container for all payment request related types
namespace paymentRequest {
    interface DisplayItem {
        amount: number;
        label: string;
        pending?: boolean;
    }

    interface StripePaymentRequestUpdateOptions {
        currency: string;
        total: DisplayItem;
        displayItems?: DisplayItem[];
        shippingOptions?: ShippingOption[];
    }

    interface StripePaymentRequestOptions extends StripePaymentRequestUpdateOptions {
        country: string;
        requestPayerName?: boolean;
        requestPayerEmail?: boolean;
        requestPayerPhone?: boolean;
        requestShipping?: boolean;
    }

    interface UpdateDetails {
        status: 'success' | 'fail' | 'invalid_shipping_address';
        total?: DisplayItem;
        displayItems?: DisplayItem[];
        shippingOptions?: ShippingOption[];
    }

    interface ShippingOption {
        id: string;
        label: string;
        detail?: string;
        amount: number;
    }

    interface ShippingAddress {
        country: string;
        addressLine: string[];
        region: string;
        city: string;
        postalCode: string;
        recipient: string;
        phone: string;
        sortingCode?: string;
        dependentLocality?: string;
    }

    interface StripePaymentResponse {
        complete: (status: string) => void;
        payerName?: string;
        payerEmail?: string;
        payerPhone?: string;
        shippingAddress?: ShippingAddress;
        shippingOption?: ShippingOption;
        methodName: string;
    }

    interface StripeTokenPaymentResponse extends StripePaymentResponse {
        token: Token;
    }

    interface StripeSourcePaymentResponse extends StripePaymentResponse {
        source: Source;
    }

    interface StripePaymentRequest {
        canMakePayment(): Promise<{applePay?: boolean} | null>;
        show(): void;
        update(options: StripePaymentRequestUpdateOptions): void;
        on(event: 'token', handler: (response: StripeTokenPaymentResponse) => void): void;
        on(event: 'source', handler: (response: StripeSourcePaymentResponse) => void): void;
        on(event: 'cancel', handler: () => void): void;
        on(event: 'shippingaddresschange', handler: (response: {updateWith: (options: UpdateDetails) => void, shippingAddress: ShippingAddress}) => void): void;
        on(event: 'shippingoptionchange', handler: (response: {updateWith: (options: UpdateDetails) => void, shippingOption: ShippingOption}) => void): void;
    }
}

 */
