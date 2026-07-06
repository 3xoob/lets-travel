import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { UserService } from '../../../core/services/user.service';
import { UserDto, UserRole } from '../../../core/models';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatButtonModule, MatIconModule, MatTableModule, MatSelectModule, MatPaginatorModule],
  template: `
    <h1>User Management</h1>
    <mat-card>
      <mat-card-content>
        <table mat-table [dataSource]="users()" class="full-width">
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef>Name</th>
            <td mat-cell *matCellDef="let u">{{ u.firstName }} {{ u.lastName }}</td>
          </ng-container>
          <ng-container matColumnDef="email">
            <th mat-header-cell *matHeaderCellDef>Email</th>
            <td mat-cell *matCellDef="let u">{{ u.email }}</td>
          </ng-container>
          <ng-container matColumnDef="role">
            <th mat-header-cell *matHeaderCellDef>Role</th>
            <td mat-cell *matCellDef="let u">
              <span class="badge" [class]="roleBadge(u.role)">{{ u.role }}</span>
            </td>
          </ng-container>
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Status</th>
            <td mat-cell *matCellDef="let u">
              <span class="badge" [class]="u.isActive ? 'badge-success' : 'badge-error'">
                {{ u.isActive ? 'Active' : 'Inactive' }}
              </span>
            </td>
          </ng-container>
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef>Change Role</th>
            <td mat-cell *matCellDef="let u">
              <mat-select [ngModel]="u.role" (ngModelChange)="changeRole(u, $event)" class="role-select">
                <mat-option value="TRAVELER">Traveler</mat-option>
                <mat-option value="MANAGER">Manager</mat-option>
                <mat-option value="ADMIN">Admin</mat-option>
              </mat-select>
            </td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="cols"></tr>
          <tr mat-row *matRowDef="let row; columns: cols;"></tr>
        </table>
      </mat-card-content>
    </mat-card>
    <mat-paginator [length]="total()" [pageSize]="20" (page)="onPage($event)" />
  `,
  styles: [`
    h1 { font-size: 28px; font-weight: 700; margin-bottom: 24px; }
    .full-width { width: 100%; }
    .role-select { width: 120px; }
  `],
})
export class AdminUsersComponent implements OnInit {
  private userService = inject(UserService);
  private snack = inject(MatSnackBar);
  users = signal<UserDto[]>([]);
  total = signal(0);
  page = 0;
  cols = ['name', 'email', 'role', 'status', 'actions'];

  ngOnInit() { this.load(); }

  load() {
    this.userService.getAllUsers(this.page).subscribe(r => {
      this.users.set(r.content);
      this.total.set(r.totalElements);
    });
  }

  onPage(e: PageEvent) { this.page = e.pageIndex; this.load(); }

  changeRole(u: UserDto, role: UserRole) {
    if (role === u.role) return;
    this.userService.changeRole(u.id, role).subscribe({
      next: updated => {
        this.users.update(list => list.map(x => x.id === updated.id ? updated : x));
        this.snack.open(`${u.email} is now ${role}`, 'OK', { duration: 3000 });
      },
      error: err => this.snack.open(err.error?.message ?? 'Failed', 'Close'),
    });
  }

  roleBadge(r: string) {
    const m: Record<string, string> = { ADMIN: 'badge-error', MANAGER: 'badge-info', TRAVELER: 'badge-success' };
    return `badge ${m[r] ?? 'badge-default'}`;
  }
}
