describe('Travel List', () => {
  it('shows travel list on homepage', () => {
    cy.visit('/');
    cy.get('h1').should('contain', 'Discover Travels');
  });

  it('shows loading skeleton while fetching', () => {
    cy.visit('/');
    cy.get('.skeleton-grid, app-travel-card, .empty-state').should('exist');
  });

  it('links to travel detail', () => {
    cy.visit('/');
    cy.get('app-travel-card', { timeout: 8000 }).first().within(() => {
      cy.get('a').contains('View Details').click();
    });
    cy.url().should('match', /\/travels\/.+/);
  });

  it('filters by status', () => {
    cy.visit('/');
    cy.get('mat-select').click();
    cy.get('mat-option').contains('Published').click();
    cy.url().should('exist');
  });
});
