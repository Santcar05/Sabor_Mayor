export type LoyaltyLevel =
  | 'ALUMNO_CULINARIO'
  | 'COCINERO_AMATEUR'
  | 'CHEF_INVITADO'
  | 'MAESTRO_SABOR';

/** GET /api/loyalty/me */
export interface LoyaltyAccount {
  points: number;
  lifetimePoints: number;
  level: LoyaltyLevel;
  nextLevel?: LoyaltyLevel;
  pointsToNextLevel?: number;
}

export type LoyaltyTransactionType = 'EARN' | 'REDEEM';

export interface LoyaltyTransaction {
  id: string;
  type: LoyaltyTransactionType;
  points: number;
  orderId?: string;
  description?: string;
  createdAt: string;
}

export const LEVEL_LABELS: Record<LoyaltyLevel, string> = {
  ALUMNO_CULINARIO: 'Alumno Culinario',
  COCINERO_AMATEUR: 'Cocinero Amateur',
  CHEF_INVITADO: 'Chef Invitado',
  MAESTRO_SABOR: 'Maestro Sabor',
};

export const LEVEL_THRESHOLDS: Record<LoyaltyLevel, number> = {
  ALUMNO_CULINARIO: 0,
  COCINERO_AMATEUR: 500,
  CHEF_INVITADO: 2000,
  MAESTRO_SABOR: 5000,
};
