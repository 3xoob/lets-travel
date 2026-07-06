export type UserRole = 'ADMIN' | 'MANAGER' | 'TRAVELER';
export type TravelStatus = 'DRAFT' | 'PUBLISHED' | 'COMPLETED' | 'CANCELLED';
export type TravelCategory = 'ADVENTURE' | 'CULTURAL' | 'RELAXATION' | 'BUSINESS' | 'SPORT' | 'NATURE' | 'CITY_BREAK' | 'CRUISE';
export type SubscriptionStatus = 'PENDING' | 'ACTIVE' | 'CANCELLED' | 'EXPIRED';
export type PaymentMethod = 'STRIPE' | 'PAYPAL';
export type PaymentStatus = 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED';
export type ReportStatus = 'OPEN' | 'RESOLVED' | 'DISMISSED';
export type ReportTargetType = 'MANAGER' | 'TRAVEL';

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: UserDto;
}

export interface UserDto {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  profileImageUrl?: string;
  isActive: boolean;
}

export interface TravelSummary {
  id: string;
  title: string;
  destination: string;
  category: TravelCategory;
  status: TravelStatus;
  price: number;
  startDate: string;
  endDate: string;
  capacity: number;
  currentEnrollment: number;
  thumbnailUrl?: string;
  managerName: string;
}

export interface TravelResponse extends TravelSummary {
  description: string;
  country: string;
  imageUrls: string[];
  tags: string[];
  manager: ManagerSummary;
  latitude?: number;
  longitude?: number;
  canSubscribe: boolean;
  canUnsubscribe: boolean;
}

export interface ManagerSummary {
  id: string;
  firstName: string;
  lastName: string;
  profileImageUrl?: string;
  averageRating: number;
  totalTrips: number;
}

export interface ManagerProfileResponse {
  userId: string;
  bio?: string;
  averageRating: number;
  totalIncome: number;
  totalTrips: number;
  reportCount: number;
  user: UserDto;
}

export interface SubscriptionResponse {
  id: string;
  travelId: string;
  travelTitle: string;
  status: SubscriptionStatus;
  payment?: PaymentSummary;
  subscribedAt: string;
  startDate: string;
  endDate: string;
}

export interface PaymentSummary {
  id: string;
  amount: number;
  method: PaymentMethod;
  status: PaymentStatus;
  paidAt?: string;
}

export interface PaymentInitResponse {
  subscriptionId: string;
  paymentId: string;
  method: PaymentMethod;
  clientSecret?: string;
  approvalUrl?: string;
}

export interface FeedbackResponse {
  id: string;
  travelId: string;
  travelTitle: string;
  travelerDisplayName: string;
  rating: number;
  comment?: string;
  createdAt: string;
}

export interface ReportResponse {
  id: string;
  targetType: ReportTargetType;
  targetId: string;
  reason: string;
  description?: string;
  status: ReportStatus;
  reporterEmail: string;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface SearchFilters {
  query?: string;
  category?: TravelCategory;
  destination?: string;
  minPrice?: number;
  maxPrice?: number;
  startDateFrom?: string;
  startDateTo?: string;
  page?: number;
  size?: number;
}

export interface IncomeByMonthDto {
  year: number;
  month: number;
  totalAmount: number;
  subscriptionCount: number;
}

export interface ManagerRankDto {
  userId: string;
  firstName: string;
  lastName: string;
  averageRating: number;
  totalIncome: number;
  totalTrips: number;
  reportCount: number;
  score: number;
}

export interface AdminDashboardResponse {
  incomeByMonth: IncomeByMonthDto[];
  topManagers: ManagerRankDto[];
  topTravels: TravelSummary[];
  recentFeedback: FeedbackResponse[];
  openReports: number;
  totalUsers: number;
  totalTravels: number;
}

export interface ManagerDashboardResponse {
  currentMonthIncome: number;
  prevMonthIncome: number;
  activeTravels: number;
  totalTravels: number;
  totalSubscribers: number;
  averageRating: number;
  travels: TravelWithStats[];
}

export interface TravelWithStats {
  travel: TravelSummary;
  subscriberCount: number;
  income: number;
  averageRating: number;
}

export interface TravelerStatsResponse {
  pastTrips: number;
  upcomingTrips: number;
  totalSpend: number;
  cancellations: number;
  reviewsGiven: number;
  reportsFiled: number;
}
