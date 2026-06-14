/** user-service: perfil del cliente, direcciones y métodos de pago. */
export interface CustomerProfile {
  id: string;
  email: string;
  fullName: string;
  phone?: string;
  dietaryPreferences: string[];
  allergies: string[];
  frequentGuest: boolean;
  visits: number;
  createdAt: string;
}

export interface UpdateProfileRequest {
  fullName: string;
  phone?: string;
  dietaryPreferences: string[];
  allergies: string[];
}

export interface Address {
  id: string;
  customerId: string;
  label: string;
  street: string;
  city: string;
  notes?: string;
  defaultAddress: boolean;
}

export interface PaymentMethodRef {
  id: string;
  customerId: string;
  gatewayToken: string;
  brand: string;
  last4: string;
}
