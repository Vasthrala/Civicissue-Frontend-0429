"""add resolution_image and notification image_url

Revision ID: a1b2c3d4e5f6
Revises: 5ca2872c8fb9
Create Date: 2026-03-17 00:00:00.000000

"""
from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision = 'a1b2c3d4e5f6'
down_revision = '5ca2872c8fb9'
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.add_column('complaints', sa.Column('resolution_image', sa.String(length=500), nullable=True))
    op.add_column('notifications', sa.Column('image_url', sa.String(length=500), nullable=True))


def downgrade() -> None:
    op.drop_column('notifications', 'image_url')
    op.drop_column('complaints', 'resolution_image')
