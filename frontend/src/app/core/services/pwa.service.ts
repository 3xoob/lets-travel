import { Injectable, inject } from '@angular/core';
import { SwUpdate, VersionReadyEvent } from '@angular/service-worker';
import { MatSnackBar } from '@angular/material/snack-bar';
import { filter } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class PwaService {
  private swUpdate = inject(SwUpdate);
  private snack = inject(MatSnackBar);

  init(): void {
    if (!this.swUpdate.isEnabled) return;

    this.swUpdate.versionUpdates.pipe(
      filter((e): e is VersionReadyEvent => e.type === 'VERSION_READY')
    ).subscribe(() => {
      const snackRef = this.snack.open(
        'A new version of Let\'s Travel is available!',
        'Update',
        { duration: 10000 }
      );
      snackRef.onAction().subscribe(() => {
        this.swUpdate.activateUpdate().then(() => document.location.reload());
      });
    });

    this.swUpdate.checkForUpdate();
  }
}
