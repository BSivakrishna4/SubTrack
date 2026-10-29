import React from 'react';
import { CheckCircle2, Clock, XCircle } from 'lucide-react';

export default function StatusBadge({ status }) {
  const normalized = status ? status.toUpperCase() : 'UNKNOWN';

  if (normalized === 'ACTIVE') {
    return (
      <span className="badge badge-active">
        <CheckCircle2 size={14} /> Active
      </span>
    );
  }

  if (normalized === 'EXPIRED') {
    return (
      <span className="badge badge-expired">
        <Clock size={14} /> Expired
      </span>
    );
  }

  if (normalized === 'CANCELLED') {
    return (
      <span className="badge badge-cancelled">
        <XCircle size={14} /> Cancelled
      </span>
    );
  }

  return (
    <span className="badge badge-secondary">
      {normalized}
    </span>
  );
}
