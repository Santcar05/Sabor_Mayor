import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-contact-reservation-section',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './contact-reservation-section.html',
  styleUrl: './contact-reservation-section.scss',
})
export class ContactReservationSectionComponent {
  formData = {
    name: '',
    email: '',
    date: '',
    time: '',
    guests: '',
    message: '',
  };

  onSubmit() {
    console.log('Form submitted:', this.formData);
  }
}
