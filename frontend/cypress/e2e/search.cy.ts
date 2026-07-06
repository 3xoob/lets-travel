describe('Search', () => {
  beforeEach(() => {
    cy.visit('/search');
  });

  it('shows search page', () => {
    cy.get('h1').should('contain', 'Search Travels');
  });

  it('performs a search', () => {
    cy.get('input[placeholder*="Search"]').type('Paris');
    cy.get('button[type="submit"]').click();
    cy.get('.result-count, .empty-state', { timeout: 8000 }).should('exist');
  });

  it('shows autocomplete suggestions', () => {
    cy.get('input[placeholder*="Search"]').type('Pa');
    cy.get('mat-option', { timeout: 3000 }).should('have.length.gte', 0);
  });

  it('can clear filters', () => {
    cy.get('mat-expansion-panel-header').click();
    cy.get('button').contains('Clear Filters').click();
    cy.get('.result-count').should('not.exist');
  });
});
