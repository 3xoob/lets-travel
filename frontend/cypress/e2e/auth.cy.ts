describe('Authentication', () => {
  beforeEach(() => {
    cy.visit('/login');
  });

  it('shows login form', () => {
    cy.get('mat-card-title').should('contain', 'Welcome back');
    cy.get('input[type="email"]').should('exist');
    cy.get('input[type="password"]').should('exist');
    cy.get('button[type="submit"]').should('contain', 'Sign In');
  });

  it('shows validation errors for empty submit', () => {
    cy.get('button[type="submit"]').click();
    cy.get('mat-error').should('exist');
  });

  it('shows error for invalid credentials', () => {
    cy.get('input[type="email"]').type('wrong@example.com');
    cy.get('input[type="password"]').type('wrongpassword');
    cy.get('button[type="submit"]').click();
    cy.get('.error-banner', { timeout: 5000 }).should('exist');
  });

  it('navigates to register page', () => {
    cy.get('a').contains('Sign up').click();
    cy.url().should('include', '/register');
  });

  it('logs in successfully with valid credentials', () => {
    cy.get('input[type="email"]').type('admin@letstravel.com');
    cy.get('input[type="password"]').type('Admin@1234');
    cy.get('button[type="submit"]').click();
    cy.url({ timeout: 5000 }).should('eq', Cypress.config().baseUrl + '/');
    cy.get('mat-toolbar').should('contain', 'account_circle');
  });
});
