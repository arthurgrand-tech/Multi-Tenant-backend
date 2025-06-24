Add this to your frontend (src/main/resources/static/js/payment.js):

class StripePaymentManager {
    constructor(publishableKey) {
        this.stripe = Stripe(publishableKey);
        this.elements = null;
        this.paymentElement = null;
    }

    async createSubscription(planId, customerEmail, customerName) {
        try {
            const response = await fetch('/api/v1/payment/subscription/create', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('jwt_token')}`,
                    'X-User-Type': 'TENANT',
                    'X-Tenant-ID': localStorage.getItem('tenant_domain')
                },
                body: JSON.stringify({
                    planId: planId,
                    customerEmail: customerEmail,
                    customerName: customerName
                })
            });

            const data = await response.json();

            if (data.response.requiresPaymentMethod) {
                await this.setupPaymentForm(data.response.clientSecret);
                return { success: true, requiresPayment: true };
            } else {
                return { success: true, requiresPayment: false };
            }

        } catch (error) {
            console.error('Failed to create subscription:', error);
            return { success: false, error: error.message };
        }
    }

    async setupPaymentForm(clientSecret) {
        this.elements = this.stripe.elements({ clientSecret });
        this.paymentElement = this.elements.create('payment');
        this.paymentElement.mount('#payment-element');
    }

    async confirmPayment() {
        if (!this.elements) {
            throw new Error('Payment form not initialized');
        }

        const { error } = await this.stripe.confirmPayment({
            elements: this.elements,
            confirmParams: {
                return_url: `${window.location.origin}/subscription-success`,
            },
        });

        if (error) {
            throw new Error(error.message);
        }
    }

    async getSubscriptionStatus() {
        const response = await fetch('/api/v1/payment/subscription/status', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('jwt_token')}`,
                'X-User-Type': 'TENANT',
                'X-Tenant-ID': localStorage.getItem('tenant_domain')
            }
        });

        return await response.json();
    }
}

// Usage example:
const paymentManager = new StripePaymentManager('pk_test_your_publishable_key');

document.getElementById('subscribe-btn').addEventListener('click', async () => {
    const result = await paymentManager.createSubscription(
        'price_premium_monthly',
        'user@example.com',
        'John Doe'
    );

    if (result.success && result.requiresPayment) {
        document.getElementById('payment-form').style.display = 'block';
    }
});

document.getElementById('confirm-payment-btn').addEventListener('click', async () => {
    try {
        await paymentManager.confirmPayment();
        alert('Payment successful!');
    } catch (error) {
        alert('Payment failed: ' + error.message);
    }
});
*/