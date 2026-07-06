describe('Admin Portal', () => {
  beforeEach(() => {
    cy.loginAsAdmin();
    cy.visit('/admin/dashboard');
  });

  it('shows admin dashboard', () => {
    cy.get('h1').should('contain', 'Admin Dashboard');
    cy.get('.kpi-grid', { timeout: 8000 }).should('exist');
  });

  it('shows KPI cards', () => {
    cy.get('.kpi', { timeout: 8000 }).should('have.length.gte', 4);
  });

  it('navigates to reports page', () => {
    cy.visit('/admin/reports');
    cy.get('h1').should('contain', 'Reports');
  });

  it('navigates to users page', () => {
    cy.visit('/admin/users');
    cy.get('h1').should('contain', 'User Management');
  });

  it('users page shows a table', () => {
    cy.visit('/admin/users');
    cy.get('table', { timeout: 8000 }).should('exist');
  });
});
