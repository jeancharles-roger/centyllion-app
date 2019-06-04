package stripe.elements

import org.w3c.dom.HTMLElement


external class ElementsOptions {
    /*
    classes?: {
        base?: string;
        complete?: string;
        empty?: string;
        focus?: string;
        invalid?: string;
        webkitAutofill?: string;
    };
     */

    var hidePostalCode: Boolean? = definedExternally
    var hideIcon: Boolean? = definedExternally

    // TODO var iconStyle?: 'solid' | 'default';
    var placeholder: String = definedExternally
    var placeholderCountry: String = definedExternally

    /* TODO
    val style?: {
        base?: Style;
        complete?: Style;
        empty?: Style;
        invalid?: Style;
        paymentRequestButton?: PaymentRequestButtonStyleOptions;
    };
     */

    var value: String = definedExternally // TODO | { [objectKey: string]: string; }
    // TODO val paymentRequest?: paymentRequest.StripePaymentRequest;
    var supportedCountries: Array<String> = definedExternally
    var disabled: Boolean? = definedExternally
}

external interface ElementChangeResponse {
    val elementType: String
    val brand: String
    val complete: Boolean
    val empty: Boolean
    // TODO value?: { postalCode: string | number } | string;
    val country: String?
    val bankName: String?
    val error: Error?
}


typealias Handler = (ElementChangeResponse) -> Unit
enum class EventTypes { blur, change, focus, ready }

external interface Element {
    fun mount(domElement: HTMLElement)
    fun mount(selector: String)
    fun on(event: EventTypes, handler: Handler)
    // TODO on(event: 'click', handler: (response: {preventDefault: () => void}) => void): void;
    fun focus()
    fun blur()
    fun clear()
    fun unmount()
    fun destroy()
    fun update(options: ElementsOptions)
}

// Was a string in typescript
enum class ElementsType {
    card, cardNumber, cardExpiry, cardCvc, postalCode, paymentRequestButton, iban, idealBank
}

external interface Elements {
    fun create(type: String, options: ElementsOptions? = definedExternally): Element
}


/*
// Container for all elements related types
namespace elements {
    interface ElementsCreateOptions {
        fonts?: Font[];
        locale?: string;
    }

    interface ElementOptions {
        fonts?: Font[];
        locale?: string;
    }


    interface Style extends StyleOptions {
        ':hover'?: StyleOptions;
        ':focus'?: StyleOptions;
        '::placeholder'?: StyleOptions;
        '::selection'?: StyleOptions;
        ':-webkit-autofill'?: StyleOptions;
        '::-ms-clear'?: StyleOptions;
    }

    interface Font {
        family?: string;
        src?: string;
        display?: string;
        style?: string;
        unicodeRange?: string;
        weight?: string;
        cssSrc?: string;
    }

    interface StyleOptions {
        color?: string;
        fontFamily?: string;
        fontSize?: string;
        fontSmoothing?: string;
        fontStyle?: string;
        fontVariant?: string;
        fontWeight?: string | number;
        iconColor?: string;
        lineHeight?: string;
        letterSpacing?: string;
        textAlign?: string;
        textDecoration?: string;
        textShadow?: string;
        textTransform?: string;
    }

    interface PaymentRequestButtonStyleOptions {
        type?: 'default' | 'donate' | 'buy';
        theme: 'dark' | 'light' | 'light-outline';
        height: string;
    }
}
 */
