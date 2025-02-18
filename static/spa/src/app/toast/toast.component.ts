import { Component, Input } from '@angular/core';
import { Toast } from '../toast.service';

@Component({
  selector: 'app-toast',
  standalone: false,
  
  templateUrl: './toast.component.html',
  styleUrl: './toast.component.css'
})
export class ToastComponent {
  @Input() toast: Toast = { id: 0, message: '', kind: 'info' };
}
