declare global {
  namespace Cypress {
    interface Chainable {
      login(email: string, password: string): void;
      loginAsAdmin(): void;
      loginAsManager(): void;
    }
  }
}

Cypress.Commands.add('login', (email: string, password: string) => {
  cy.request('POST', `${Cypress.env('apiUrl') || 'http://localhost:8080/api'}/auth/login`, {
    email, password,
  }).then(res => {
    localStorage.setItem('accessToken', res.body.accessToken);
    localStorage.setItem('refreshToken', res.body.refreshToken);
    localStorage.setItem('user', JSON.stringify(res.body.user));
  });
});

Cypress.Commands.add('loginAsAdmin', () => {
  cy.login('admin@letstravel.com', 'Admin@1234');
});

Cypress.Commands.add('loginAsManager', () => {
  cy.login('manager@letstravel.com', 'Manager@1234');
});

export {};
