import { Component } from '@angular/core';
import { Toast, ToastService } from '../toast.service';

@Component({
  selector: 'app-toast-list',
  standalone: false,
  
  templateUrl: './toast-list.component.html',
  styleUrl: './toast-list.component.css'
})
export class ToastListComponent {
  public toasts: Toast[] = [];
  
  constructor(private readonly toastService: ToastService) {}

  ngOnInit() {
    this.toastService.events.subscribe(event => {
      if (event.type === 'add') {
        this.toasts.push(event.toast);
      } else if (event.type === 'remove') {
        this.toasts = this.toasts.filter(toast => toast.id !== event.id);
      }
    });
  }
}
