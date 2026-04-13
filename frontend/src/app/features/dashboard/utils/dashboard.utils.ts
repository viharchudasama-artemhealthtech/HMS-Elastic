import { Chart, ChartConfiguration } from 'chart.js';
import { AppointmentStatus } from '../../../core/models/appointment.models';
import { DashboardSummary, WeeklyStatistics } from '../../../core/models/common.models';

export interface QuickAction {
  label: string;
  link: string;
  icon: string;
}

export function buildDashboardQuickActions(role: string): QuickAction[] {
  if (role === 'DOCTOR') {
    return [
      { label: 'Register Patient', link: '/patients/register', icon: 'ri-user-add-line' },
      { label: 'Prescriptions', link: '/prescriptions', icon: 'ri-file-list-3-line' },
    ];
  }

  if (role === 'PHARMACIST') {
    return [
      { label: 'View Prescriptions', link: '/prescriptions', icon: 'ri-file-list-3-line' },
      { label: 'Inventory', link: '/pharmacy/inventory', icon: 'ri-capsule-line' },
    ];
  }

  if (role === 'RECEPTIONIST' || role === 'ADMIN' || role === 'NURSE') {
    return [
      { label: 'Register Patient', link: '/patients/register', icon: 'ri-user-add-line' },
      { label: 'Book Appointment', link: '/appointments/book', icon: 'ri-calendar-todo-line' },
    ];
  }

  return [];
}

export function getDashboardStatusClass(status: AppointmentStatus): string {
  const map: Record<string, string> = {
    SCHEDULED: 'status-scheduled',
    CHECKED_IN: 'status-checked-in',
    IN_CONSULTATION: 'status-in-progress',
    COMPLETED: 'status-completed',
    CANCELLED: 'status-cancelled',
  };

  return map[status] || 'status-scheduled';
}

const valueLabelPlugin = {
  id: 'valueLabelPlugin',
  afterDatasetsDraw(chart: Chart) {
    const { ctx } = chart;
    ctx.save();
    ctx.textAlign = 'center';
    ctx.textBaseline = 'bottom';
    ctx.fillStyle = '#334155';
    ctx.font = '700 11px system-ui';

    chart.data.datasets.forEach((dataset, datasetIndex) => {
      const meta = chart.getDatasetMeta(datasetIndex);
      if (meta.hidden) {
        return;
      }

      meta.data.forEach((element, index) => {
        const rawValue = dataset.data[index];
        if (typeof rawValue !== 'number') {
          return;
        }

        const position = element.tooltipPosition(false);
        if (position.x == null || position.y == null) {
          return;
        }

        ctx.fillText(String(rawValue), position.x, position.y - 8);
      });
    });

    ctx.restore();
  },
};

export function createDashboardChart(ctx: CanvasRenderingContext2D, data: DashboardSummary): Chart {
  const stats: WeeklyStatistics[] = data.weeklyStats || [];
  const { labels, appointmentData, patientData } = stats.reduce(
    (acc, s) => {
      acc.labels.push(s.day);
      acc.appointmentData.push(s.appointments || 0);
      acc.patientData.push(s.patients || 0);
      return acc;
    },
    { labels: [] as string[], appointmentData: [] as number[], patientData: [] as number[] },
  );

  const config: ChartConfiguration<'bar'> = {
    type: 'bar',
    data: {
      labels,
      datasets: [
        {
          label: 'Appointments',
          data: appointmentData,
          backgroundColor: '#2563ebcc',
          borderColor: '#1d4ed8',
          borderWidth: 1,
          borderRadius: 8,
          barThickness: 26,
        },
        {
          label: 'New Patients',
          data: patientData,
          backgroundColor: '#14b8a6cc',
          borderColor: '#0f766e',
          borderWidth: 1,
          borderRadius: 8,
          barThickness: 26,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { position: 'top' },
        tooltip: { mode: 'index', intersect: false },
      },
      scales: {
        y: {
          beginAtZero: true,
          grace: 1,
          ticks: {
            precision: 0,
            stepSize: 1,
          },
          grid: { color: '#f3f4f6' },
        },
        x: { grid: { display: false } },
      },
    },
    plugins: [valueLabelPlugin],
  };

  return new Chart(ctx, config);
}

export function createDepartmentChart(ctx: CanvasRenderingContext2D, data: DashboardSummary): Chart {
  const stats = data.departmentStats || [];
  const { labels, counts } = stats.reduce(
    (acc, s) => {
      acc.labels.push(
        s.department
          .replace(/_/g, ' ')
          .toLowerCase()
          .replace(/\b\w/g, (char) => char.toUpperCase()),
      );
      acc.counts.push(s.appointmentCount);
      return acc;
    },
    { labels: [] as string[], counts: [] as number[] },
  );

  const colors = [
    '#1d4ed8',
    '#0f766e',
    '#c2410c',
    '#be123c',
    '#6d28d9',
    '#0f766e',
    '#0369a1',
    '#b45309',
    '#0f766e',
    '#4338ca',
  ];

  const config: ChartConfiguration<'bar'> = {
    type: 'bar',
    data: {
      labels,
      datasets: [
        {
          label: 'Appointments',
          data: counts,
          backgroundColor: colors.slice(0, stats.length),
          borderRadius: 8,
          borderWidth: 1,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: false,
        },
        tooltip: {
          callbacks: {
            label: (item) => ` ${item.label}: ${item.raw} appointments`,
          },
        },
      },
      scales: {
        y: {
          beginAtZero: true,
          grace: 1,
          ticks: {
            precision: 0,
            stepSize: 1,
          },
          grid: { color: '#f3f4f6' },
        },
        x: { grid: { display: false } },
      },
    },
    plugins: [valueLabelPlugin],
  };

  return new Chart(ctx, config);
}
