describe('Manager Portal', () => {
  beforeEach(() => {
    cy.loginAsManager();
    cy.visit('/manager/dashboard');
  });

  it('shows manager dashboard', () => {
    cy.get('h1').should('contain', 'Manager Dashboard');
    cy.get('.kpi-grid', { timeout: 8000 }).should('exist');
  });

  it('shows create travel button', () => {
    cy.get('a').contains('New Travel').should('exist');
  });

  it('navigates to travels list', () => {
    cy.get('a').contains('View all travels').click();
    cy.url().should('include', '/manager/travels');
    cy.get('h1').should('contain', 'My Travels');
  });

  it('can open create travel form', () => {
    cy.visit('/manager/travels/create');
    cy.get('mat-card-title').should('contain', 'Create New Travel');
  });

  it('validates required fields on create form', () => {
    cy.visit('/manager/travels/create');
    cy.get('button[type="submit"]').click();
    cy.get('mat-error').should('exist');
  });
});
